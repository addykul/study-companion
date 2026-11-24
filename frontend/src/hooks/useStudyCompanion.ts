import { useCallback, useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { api, type ChatResult, type Course, type CourseDocument, type Progress, type Quiz, type QuizSubmission, type Source } from '../api'

export type StudyTab = 'learn' | 'quiz' | 'progress'
export type ChatEntry = { id: string; role: 'user' | 'assistant'; text: string; sources?: Source[] }

const DOCUMENT_POLL_INTERVAL_MS = 5_000
const SELECTED_COURSE_STORAGE_KEY = 'study-companion:selected-course'

function errorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

export function useStudyCompanion() {
  const [courses, setCourses] = useState<Course[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(() =>
    window.localStorage.getItem(SELECTED_COURSE_STORAGE_KEY))
  const [documents, setDocuments] = useState<CourseDocument[]>([])
  const [progress, setProgress] = useState<Progress | null>(null)
  const [tab, setTab] = useState<StudyTab>('learn')
  const [loadingCourses, setLoadingCourses] = useState(true)
  const [pageError, setPageError] = useState('')

  const [showCreate, setShowCreate] = useState(false)
  const [newName, setNewName] = useState('')
  const [newDescription, setNewDescription] = useState('')
  const [creating, setCreating] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [selectedFileName, setSelectedFileName] = useState('')

  const [question, setQuestion] = useState('')
  const [asking, setAsking] = useState(false)
  const [loadingChat, setLoadingChat] = useState(false)
  const [conversationId, setConversationId] = useState<string | null>(null)
  const [chat, setChat] = useState<ChatEntry[]>([])
  const [activeSource, setActiveSource] = useState<Source | null>(null)

  const [topic, setTopic] = useState('')
  const [quiz, setQuiz] = useState<Quiz | null>(null)
  const [answers, setAnswers] = useState<Record<string, number>>({})
  const [submission, setSubmission] = useState<QuizSubmission | null>(null)
  const [creatingQuiz, setCreatingQuiz] = useState(false)
  const [submittingQuiz, setSubmittingQuiz] = useState(false)
  const [quizError, setQuizError] = useState('')

  const selectedIdRef = useRef(selectedId)
  const documentRequestRef = useRef(0)
  const chatRequestRef = useRef(0)
  const quizRequestRef = useRef(0)
  selectedIdRef.current = selectedId

  const selectedCourse = useMemo(
    () => courses.find((course) => course.id === selectedId) ?? null,
    [courses, selectedId],
  )
  const readyDocuments = useMemo(
    () => documents.filter((document) => document.status === 'READY').length,
    [documents],
  )

  const loadCourses = useCallback(async () => {
    try {
      const loaded = await api.courses()
      setCourses(loaded)
      setSelectedId((current) => current && loaded.some((course) => course.id === current)
        ? current
        : loaded[0]?.id ?? null)
      setPageError('')
    } catch (error) {
      setPageError(errorMessage(error, 'Could not load courses.'))
    } finally {
      setLoadingCourses(false)
    }
  }, [])

  const loadDocuments = useCallback(async (courseId: string) => {
    const requestId = ++documentRequestRef.current
    try {
      const loaded = await api.documents(courseId)
      if (requestId === documentRequestRef.current && selectedIdRef.current === courseId) {
        setDocuments(loaded)
      }
    } catch (error) {
      if (selectedIdRef.current === courseId) {
        setPageError(errorMessage(error, 'Could not load documents.'))
      }
    }
  }, [])

  useEffect(() => { void loadCourses() }, [loadCourses])

  useEffect(() => {
    if (selectedId) window.localStorage.setItem(SELECTED_COURSE_STORAGE_KEY, selectedId)
    else window.localStorage.removeItem(SELECTED_COURSE_STORAGE_KEY)
  }, [selectedId])

  useEffect(() => {
    documentRequestRef.current += 1
    chatRequestRef.current += 1
    quizRequestRef.current += 1
    setProgress(null)
    setDocuments([])
    setChat([])
    setConversationId(null)
    setActiveSource(null)
    setQuiz(null)
    setSubmission(null)
    setAnswers({})
    setAsking(false)
    setLoadingChat(Boolean(selectedId))
    setCreatingQuiz(false)
    setSubmittingQuiz(false)
    if (selectedId) {
      void loadDocuments(selectedId)
      const requestId = chatRequestRef.current
      api.chatHistory(selectedId).then((history) => {
        if (chatRequestRef.current !== requestId || selectedIdRef.current !== selectedId) return
        setConversationId(history.conversationId)
        setChat(history.messages.map((message) => ({
          id: message.id,
          role: message.role,
          text: message.content,
          sources: message.sources,
        })))
      }).catch((error: unknown) => {
        if (chatRequestRef.current === requestId && selectedIdRef.current === selectedId) {
          setPageError(errorMessage(error, 'Could not load chat history.'))
        }
      }).finally(() => {
        if (chatRequestRef.current === requestId && selectedIdRef.current === selectedId) {
          setLoadingChat(false)
        }
      })
    }
  }, [selectedId, loadDocuments])

  useEffect(() => {
    if (!selectedId || !documents.some((document) => document.status === 'PROCESSING')) return
    const timer = window.setInterval(() => { void loadDocuments(selectedId) }, DOCUMENT_POLL_INTERVAL_MS)
    return () => window.clearInterval(timer)
  }, [selectedId, documents, loadDocuments])

  useEffect(() => {
    if (!selectedId || tab !== 'progress') return
    let active = true
    api.progress(selectedId).then((result) => {
      if (active && selectedIdRef.current === selectedId) setProgress(result)
    }).catch((error: unknown) => {
      if (active && selectedIdRef.current === selectedId) {
        setPageError(errorMessage(error, 'Could not load progress.'))
      }
    })
    return () => { active = false }
  }, [selectedId, tab, submission])

  const selectCourse = useCallback((courseId: string) => {
    setSelectedId(courseId)
    setTab('learn')
  }, [])

  async function handleCreateCourse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const name = newName.trim()
    if (!name || creating) return
    setCreating(true)
    try {
      const course = await api.createCourse(name, newDescription.trim())
      setCourses((current) => [course, ...current])
      setSelectedId(course.id)
      setNewName('')
      setNewDescription('')
      setShowCreate(false)
      setTab('learn')
      setPageError('')
    } catch (error) {
      setPageError(errorMessage(error, 'Could not create course.'))
    } finally {
      setCreating(false)
    }
  }

  async function handleUpload(event: FormEvent<HTMLFormElement>, file: File | undefined, input: HTMLInputElement | null) {
    event.preventDefault()
    if (!file || !selectedId || uploading) return
    setUploading(true)
    try {
      const document = await api.upload(selectedId, file)
      setDocuments((current) => selectedIdRef.current === selectedId ? [document, ...current] : current)
      if (input) input.value = ''
      setSelectedFileName('')
      setPageError('')
      void loadCourses()
    } catch (error) {
      setPageError(errorMessage(error, 'Could not upload file.'))
    } finally {
      setUploading(false)
    }
  }

  async function handleAsk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedId || !question.trim() || asking) return
    const prompt = question.trim()
    const courseId = selectedId
    const requestConversationId = conversationId
    const requestId = ++chatRequestRef.current
    const entryId = `chat-${Date.now()}`
    setQuestion('')
    setChat((current) => [...current, { id: `${entryId}-user`, role: 'user', text: prompt }])
    setAsking(true)
    try {
      const result: ChatResult = await api.ask(courseId, prompt, requestConversationId)
      if (chatRequestRef.current !== requestId || selectedIdRef.current !== courseId) return
      setConversationId(result.conversationId)
      setChat((current) => [...current, { id: `${entryId}-assistant`, role: 'assistant', text: result.answer, sources: result.sources }])
      setPageError('')
    } catch (error) {
      if (chatRequestRef.current === requestId && selectedIdRef.current === courseId) {
        setChat((current) => [...current, {
          id: `${entryId}-assistant-error`,
          role: 'assistant',
          text: errorMessage(error, 'Could not get an answer.'),
        }])
      }
    } finally {
      if (chatRequestRef.current === requestId) setAsking(false)
    }
  }

  async function handleCreateQuiz(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedId || creatingQuiz) return
    const courseId = selectedId
    const requestId = ++quizRequestRef.current
    setCreatingQuiz(true)
    setQuizError('')
    setQuiz(null)
    setSubmission(null)
    setAnswers({})
    try {
      const generatedQuiz = await api.createQuiz(courseId, topic.trim())
      if (quizRequestRef.current === requestId && selectedIdRef.current === courseId) setQuiz(generatedQuiz)
    } catch (error) {
      if (quizRequestRef.current === requestId && selectedIdRef.current === courseId) {
        setQuizError(errorMessage(error, 'Could not generate a quiz.'))
      }
    } finally {
      if (quizRequestRef.current === requestId) setCreatingQuiz(false)
    }
  }

  async function handleSubmitQuiz() {
    if (!quiz || submittingQuiz) return
    const requestId = ++quizRequestRef.current
    const quizId = quiz.id
    setSubmittingQuiz(true)
    setQuizError('')
    try {
      const result = await api.submitQuiz(quizId, answers)
      if (quizRequestRef.current === requestId) setSubmission(result)
    } catch (error) {
      if (quizRequestRef.current === requestId) setQuizError(errorMessage(error, 'Could not submit answers.'))
    } finally {
      if (quizRequestRef.current === requestId) setSubmittingQuiz(false)
    }
  }

  function resetQuiz() {
    quizRequestRef.current += 1
    setCreatingQuiz(false)
    setSubmittingQuiz(false)
    setQuiz(null)
    setSubmission(null)
    setAnswers({})
    setQuizError('')
  }

  return {
    courses, selectedId, selectedCourse, documents, readyDocuments, progress, tab,
    loadingCourses, pageError, setPageError, showCreate, setShowCreate,
    newName, setNewName, newDescription, setNewDescription, creating,
    uploading, selectedFileName, setSelectedFileName, question, setQuestion,
    asking, loadingChat, chat, activeSource, setActiveSource, topic, setTopic, quiz, answers,
    setAnswers, submission, creatingQuiz, submittingQuiz, quizError,
    setTab, selectCourse, handleCreateCourse, handleUpload, handleAsk,
    handleCreateQuiz, handleSubmitQuiz, resetQuiz,
  }
}
