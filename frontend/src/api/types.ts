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
export type ChatHistory = {
  conversationId: string | null
  messages: { id: string; role: 'user' | 'assistant'; content: string; createdAt: string; sources: Source[] }[]
}
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
