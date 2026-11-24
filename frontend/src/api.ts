export type Course = {
  id: string
  name: string
  description: string
  createdAt: string
  documentCount: number
}

export type CourseDocument = {
  id: string
  courseId: string
  fileName: string
  contentType: string
  status: 'PROCESSING' | 'READY' | 'FAILED'
  errorMessage: string | null
  createdAt: string
}

export type Source = { documentId: string; documentName: string; page: number | null; excerpt: string }
export type ChatResult = { conversationId: string; answer: string; sources: Source[] }
export type QuizQuestion = { id: string; topic: string; prompt: string; options: string[] }
export type Quiz = { id: string; courseId: string; title: string; topic: string | null; questions: QuizQuestion[]; createdAt: string }
export type QuizSubmission = {
  id: string
  quizId: string
  score: number
  totalQuestions: number
  results: { questionId: string; correct: boolean; selectedIndex: number | null; correctIndex: number; explanation: string; topic: string }[]
  createdAt: string
}
export type Progress = {
  quizzesTaken: number
  averageScorePercent: number
  topics: { topic: string; correct: number; total: number; accuracy: number }[]
  reviewSuggestions: string[]
}

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API}${path}`, init)
  } catch {
    throw new Error('Could not connect to the API. Make sure the backend is running on port 8080.')
  }
  if (!response.ok) {
    let message = `Request failed (${response.status}).`
    try {
      const body = await response.json() as { message?: string }
      if (body.message) message = body.message
    } catch { /* keep the status message */ }
    throw new Error(message)
  }
  return response.json() as Promise<T>
}

export const api = {
  courses: () => request<Course[]>('/courses'),
  createCourse: (name: string, description: string) => request<Course>('/courses', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name, description }),
  }),
  documents: (courseId: string) => request<CourseDocument[]>(`/courses/${courseId}/documents`),
  upload: (courseId: string, file: File) => {
    const data = new FormData()
    data.append('file', file)
    return request<CourseDocument>(`/courses/${courseId}/documents`, { method: 'POST', body: data })
  },
  ask: (courseId: string, question: string, conversationId: string | null) => request<ChatResult>(`/courses/${courseId}/chat`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ question, conversationId }),
  }),
  createQuiz: (courseId: string, topic: string) => request<Quiz>(`/courses/${courseId}/quizzes`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ topic: topic || null }),
  }),
  submitQuiz: (quizId: string, answers: Record<string, number>) => request<QuizSubmission>(`/quizzes/${quizId}/submissions`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ answers }),
  }),
  progress: (courseId: string) => request<Progress>(`/courses/${courseId}/progress`),
}
