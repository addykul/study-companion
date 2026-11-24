import { createHttpClient } from './httpClient'
import type { ChatResult, Course, CourseDocument, Progress, Quiz, QuizSubmission } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const request = createHttpClient(API_BASE_URL)
const jsonHeaders = { 'Content-Type': 'application/json' }
const encodeId = (id: string) => encodeURIComponent(id)

export const api = {
  courses: () => request<Course[]>('/courses'),
  createCourse: (name: string, description: string) => request<Course>('/courses', {
    method: 'POST', headers: jsonHeaders, body: JSON.stringify({ name, description }),
  }),
  documents: (courseId: string) => request<CourseDocument[]>(`/courses/${encodeId(courseId)}/documents`),
  upload: (courseId: string, file: File) => {
    const data = new FormData()
    data.append('file', file)
    return request<CourseDocument>(`/courses/${encodeId(courseId)}/documents`, { method: 'POST', body: data })
  },
  ask: (courseId: string, question: string, conversationId: string | null) => request<ChatResult>(`/courses/${encodeId(courseId)}/chat`, {
    method: 'POST', headers: jsonHeaders, body: JSON.stringify({ question, conversationId }),
  }),
  createQuiz: (courseId: string, topic: string) => request<Quiz>(`/courses/${encodeId(courseId)}/quizzes`, {
    method: 'POST', headers: jsonHeaders, body: JSON.stringify({ topic: topic || null }),
  }),
  submitQuiz: (quizId: string, answers: Record<string, number>) => request<QuizSubmission>(`/quizzes/${encodeId(quizId)}/submissions`, {
    method: 'POST', headers: jsonHeaders, body: JSON.stringify({ answers }),
  }),
  progress: (courseId: string) => request<Progress>(`/courses/${encodeId(courseId)}/progress`),
}
