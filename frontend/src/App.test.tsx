import { cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { api, type Course, type Quiz } from './api'

vi.mock('./api', () => ({
  api: {
    courses: vi.fn(), createCourse: vi.fn(), documents: vi.fn(), upload: vi.fn(), ask: vi.fn(),
    createQuiz: vi.fn(), submitQuiz: vi.fn(), progress: vi.fn(),
  },
}))

const course: Course = {
  id: 'course-1', name: 'Learning AI', description: 'Foundations and concepts',
  createdAt: '2026-11-20T10:00:00Z', documentCount: 0,
}

const quiz: Quiz = {
  id: 'quiz-1', courseId: course.id, title: 'Practice: embeddings', topic: 'embeddings',
  createdAt: '2026-11-20T10:00:00Z',
  questions: Array.from({ length: 5 }, (_, index) => ({
    id: `question-${index + 1}`, topic: 'Embeddings', prompt: `Question ${index + 1}?`,
    options: ['Option A', 'Option B', 'Option C', 'Option D'],
  })),
}

describe('Study Companion journeys', () => {
  afterEach(() => cleanup())

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(api.courses).mockResolvedValue([])
    vi.mocked(api.documents).mockResolvedValue([])
    vi.mocked(api.createCourse).mockResolvedValue(course)
    vi.mocked(api.upload).mockResolvedValue({
      id: 'doc-1', courseId: course.id, fileName: 'week-1.txt', contentType: 'text/plain',
      status: 'READY', errorMessage: null, createdAt: '2026-11-20T10:00:00Z',
    })
    vi.mocked(api.ask).mockResolvedValue({
      conversationId: 'conversation-1', answer: 'Embeddings represent text as vectors.',
      sources: [{ documentId: 'doc-1', documentName: 'week-1.txt', page: 2, excerpt: 'Text can be represented as a vector.' }],
    })
    vi.mocked(api.createQuiz).mockResolvedValue(quiz)
    vi.mocked(api.submitQuiz).mockResolvedValue({
      id: 'submission-1', quizId: quiz.id, score: 4, totalQuestions: 5,
      results: quiz.questions.map((question, index) => ({
        questionId: question.id, correct: index < 4, selectedIndex: index === 4 ? 1 : 0,
        correctIndex: 0, explanation: 'The notes explain this concept.', topic: question.topic,
      })),
      createdAt: '2026-11-20T10:00:00Z',
    })
  })

  it('creates a course, uploads notes, and shows a cited answer', async () => {
    const user = userEvent.setup()
    vi.mocked(api.courses).mockResolvedValueOnce([]).mockResolvedValue([{ ...course, documentCount: 1 }])
    render(<App />)
    await user.click(await screen.findByRole('button', { name: /New course/i }))
    await user.type(screen.getByLabelText('Course name'), 'Learning AI')
    await user.type(screen.getByLabelText(/Description/i), 'Foundations and concepts')
    await user.click(screen.getByRole('button', { name: /Create course/i }))
    await screen.findByRole('heading', { name: 'Learning AI' })
    await screen.findByText('No material added yet.')

    const fileInput = document.querySelector<HTMLInputElement>('input[type="file"]')!
    const file = new File(['Embeddings notes'], 'week-1.txt', { type: 'text/plain' })
    await user.upload(fileInput, file)
    await user.click(screen.getByRole('button', { name: 'Upload' }))
    await screen.findByText('Ready to study')

    await user.type(screen.getByRole('textbox', { name: /Ask a question/i }), 'What are embeddings?')
    await user.click(screen.getByRole('button', { name: 'Send question' }))
    expect(await screen.findByText('Embeddings represent text as vectors.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /week-1.txt · p. 2/i }))
    expect(screen.getByText('Text can be represented as a vector.')).toBeInTheDocument()
    expect(api.upload).toHaveBeenCalledWith(course.id, file)
    expect(api.ask).toHaveBeenCalledWith(course.id, 'What are embeddings?', null)
  })

  it('generates a quiz, records answers, and displays the result', async () => {
    const user = userEvent.setup()
    vi.mocked(api.courses).mockResolvedValue([{ ...course, documentCount: 1 }])
    vi.mocked(api.documents).mockResolvedValue([{
      id: 'doc-1', courseId: course.id, fileName: 'week-1.txt', contentType: 'text/plain',
      status: 'READY', errorMessage: null, createdAt: '2026-11-20T10:00:00Z',
    }])
    render(<App />)
    await user.click(await screen.findByRole('button', { name: 'Practice' }))
    await user.type(screen.getByPlaceholderText('e.g. Gradient descent'), 'Embeddings')
    await user.click(screen.getByRole('button', { name: /Create a quiz/i }))
    expect(await screen.findByText('Question 1?')).toBeInTheDocument()
    const firstQuestion = screen.getByText('Question 1?').closest<HTMLElement>('.question-card')!
    await user.click(within(firstQuestion).getByRole('button', { name: /Option A/i }))
    await user.click(screen.getByRole('button', { name: /Check my answers/i }))
    expect(api.submitQuiz).toHaveBeenCalled()
    expect(await screen.findByText(/You got 4 of 5 correct/)).toBeInTheDocument()
    expect(screen.getAllByText('The notes explain this concept.')).toHaveLength(5)
    expect(api.createQuiz).toHaveBeenCalledWith(course.id, 'Embeddings')
    expect(api.submitQuiz).toHaveBeenCalledWith(quiz.id, expect.objectContaining({ 'question-1': 0 }))
  })
})
