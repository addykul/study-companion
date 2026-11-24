import { useCallback, useEffect, useRef, useState, type FormEvent } from 'react'
import {
  AlertCircle, ArrowLeft, ArrowRight, BookMarked, BookOpen, Check, CheckCircle2,
  ChevronDown, Clock3, FileText, GraduationCap, Layers3, LoaderCircle, MessageSquare,
  Plus, Send, Sparkles, Target, Upload, X,
} from 'lucide-react'
import { api, type ChatResult, type Course, type CourseDocument, type Progress, type Quiz, type QuizSubmission, type Source } from './api'

type Tab = 'learn' | 'quiz' | 'progress'
type ChatEntry = { role: 'user' | 'assistant'; text: string; sources?: Source[] }
const DOCUMENT_POLL_INTERVAL_MS = 5000

function App() {
  const [courses, setCourses] = useState<Course[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [documents, setDocuments] = useState<CourseDocument[]>([])
  const [progress, setProgress] = useState<Progress | null>(null)
  const [tab, setTab] = useState<Tab>('learn')
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
  const fileRef = useRef<HTMLInputElement>(null)
  const selectedCourse = courses.find((course) => course.id === selectedId) ?? null

  const loadCourses = useCallback(async () => {
    try {
      const loaded = await api.courses()
      setCourses(loaded)
      setSelectedId((current) => current && loaded.some((course) => course.id === current) ? current : loaded[0]?.id ?? null)
      setPageError('')
    } catch (error) {
      setPageError(error instanceof Error ? error.message : 'Could not load courses.')
    } finally {
      setLoadingCourses(false)
    }
  }, [])

  useEffect(() => { void loadCourses() }, [loadCourses])

  const loadDocuments = useCallback(async (courseId: string) => {
    try { setDocuments(await api.documents(courseId)) }
    catch (error) { setPageError(error instanceof Error ? error.message : 'Could not load documents.') }
  }, [])

  useEffect(() => {
    setProgress(null)
    setDocuments([])
    setChat([])
    setConversationId(null)
    setQuiz(null)
    setSubmission(null)
    if (selectedId) void loadDocuments(selectedId)
  }, [selectedId, loadDocuments])

  useEffect(() => {
    if (!selectedId || !documents.some((document) => document.status === 'PROCESSING')) return
    const timer = window.setInterval(() => { void loadDocuments(selectedId) }, DOCUMENT_POLL_INTERVAL_MS)
    return () => window.clearInterval(timer)
  }, [selectedId, documents, loadDocuments])

  useEffect(() => {
    if (!selectedId || tab !== 'progress') return
    api.progress(selectedId).then(setProgress).catch((error: unknown) => {
      setPageError(error instanceof Error ? error.message : 'Could not load progress.')
    })
  }, [selectedId, tab, submission])

  async function handleCreateCourse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!newName.trim()) return
    setCreating(true)
    try {
      const course = await api.createCourse(newName.trim(), newDescription.trim())
      setCourses((current) => [course, ...current])
      setSelectedId(course.id)
      setNewName('')
      setNewDescription('')
      setShowCreate(false)
      setTab('learn')
      setPageError('')
    } catch (error) {
      setPageError(error instanceof Error ? error.message : 'Could not create course.')
    } finally { setCreating(false) }
  }

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const file = fileRef.current?.files?.[0]
    if (!file || !selectedId) return
    setUploading(true)
    try {
      const document = await api.upload(selectedId, file)
      setDocuments((current) => [document, ...current])
      if (fileRef.current) fileRef.current.value = ''
      setSelectedFileName('')
      setPageError('')
      void loadCourses()
    } catch (error) {
      setPageError(error instanceof Error ? error.message : 'Could not upload file.')
    } finally { setUploading(false) }
  }

  async function handleAsk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedId || !question.trim() || asking) return
    const prompt = question.trim()
    setQuestion('')
    setChat((current) => [...current, { role: 'user', text: prompt }])
    setAsking(true)
    try {
      const result: ChatResult = await api.ask(selectedId, prompt, conversationId)
      setConversationId(result.conversationId)
      setChat((current) => [...current, { role: 'assistant', text: result.answer, sources: result.sources }])
      setPageError('')
    } catch (error) {
      setChat((current) => [...current, { role: 'assistant', text: error instanceof Error ? error.message : 'Could not get an answer.' }])
    } finally { setAsking(false) }
  }

  async function handleCreateQuiz(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedId) return
    setCreatingQuiz(true)
    setQuizError('')
    setQuiz(null)
    setSubmission(null)
    setAnswers({})
    try { setQuiz(await api.createQuiz(selectedId, topic.trim())) }
    catch (error) { setQuizError(error instanceof Error ? error.message : 'Could not generate a quiz.') }
    finally { setCreatingQuiz(false) }
  }

  async function handleSubmitQuiz() {
    if (!quiz) return
    setSubmittingQuiz(true)
    setQuizError('')
    try { setSubmission(await api.submitQuiz(quiz.id, answers)) }
    catch (error) { setQuizError(error instanceof Error ? error.message : 'Could not submit answers.') }
    finally { setSubmittingQuiz(false) }
  }

  const readyDocuments = documents.filter((document) => document.status === 'READY').length

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#home" onClick={(event) => event.preventDefault()}>
          <span className="brand-mark"><BookOpen size={19} strokeWidth={2.2} /></span>
          <span>study<span className="brand-light">space</span></span>
        </a>
        <div className="sidebar-section-label">YOUR LIBRARY <span className="library-count">{courses.length}</span></div>
        <div className="course-list">
          {loadingCourses && <div className="sidebar-loading"><LoaderCircle className="spin" size={15} /> Loading courses</div>}
          {!loadingCourses && courses.map((course, index) => (
            <button key={course.id} className={`course-nav ${selectedId === course.id ? 'active' : ''}`} onClick={() => { setSelectedId(course.id); setTab('learn') }}>
              <span className={`course-dot dot-${index % 4}`} />
              <span className="course-nav-copy"><span className="course-nav-name">{course.name}</span><span className="course-nav-meta">{course.documentCount} {course.documentCount === 1 ? 'document' : 'documents'}</span></span>
              {selectedId === course.id && <ChevronDown size={15} className="nav-chevron" />}
            </button>
          ))}
          {!loadingCourses && courses.length === 0 && <p className="empty-library">Your courses will appear here.</p>}
        </div>
        <button className="new-course-button" onClick={() => setShowCreate(true)}><Plus size={16} /> New course</button>
        <div className="sidebar-bottom">
          <div className="sidebar-tip-icon"><Sparkles size={15} /></div>
          <p>Small steps add up.<br /><span>Keep a little momentum today.</span></p>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb"><span>Workspace</span><span className="crumb-slash">/</span><strong>{selectedCourse?.name ?? 'My learning'}</strong></div>
          <div className="topbar-right"><span className="connection-dot" /> <span>Personal study space</span><span className="avatar">A</span></div>
        </header>

        <div className="content-wrap">
          {pageError && <div className="notice error-notice"><AlertCircle size={16} /><span>{pageError}</span><button aria-label="Dismiss message" onClick={() => setPageError('')}><X size={15} /></button></div>}
          {showCreate && <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setShowCreate(false) }}>
            <form className="create-modal" onSubmit={handleCreateCourse}>
              <button type="button" className="modal-close" aria-label="Close" onClick={() => setShowCreate(false)}><X size={18} /></button>
              <div className="modal-icon"><BookMarked size={20} /></div>
              <h2>Create a course</h2>
              <p>Make a dedicated space for notes, questions, and practice.</p>
              <label>Course name<input autoFocus maxLength={120} value={newName} onChange={(event) => setNewName(event.target.value)} placeholder="e.g. Introduction to machine learning" /></label>
              <label>Description <span className="optional">Optional</span><textarea maxLength={500} value={newDescription} onChange={(event) => setNewDescription(event.target.value)} placeholder="What are you working on?" rows={3} /></label>
              <button className="primary-button modal-submit" disabled={!newName.trim() || creating}>{creating ? <LoaderCircle className="spin" size={16} /> : <Plus size={16} />} Create course</button>
            </form>
          </div>}

          {!loadingCourses && courses.length === 0 ? (
            <section className="welcome-card">
              <div className="welcome-illustration"><div className="sun-shape" /><div className="book-stack"><span /><span /><span /></div><Sparkles className="welcome-spark spark-one" size={18} /><Sparkles className="welcome-spark spark-two" size={13} /></div>
              <div className="eyebrow">A calmer way to study</div>
              <h1>Your notes, ready to<br /><em>teach you back.</em></h1>
              <p>Create a course, add your study material, and ask questions grounded in your own notes. Then practice what you’ve learned with a quick quiz.</p>
              <button className="primary-button" onClick={() => setShowCreate(true)}><Plus size={17} /> Create your first course <ArrowRight size={16} /></button>
              <div className="welcome-footnote"><CheckCircle2 size={15} /> Files are stored locally; retrieved excerpts go to your configured AI provider.</div>
            </section>
          ) : selectedCourse ? (
            <>
              <section className="course-heading">
                <div className="course-heading-main">
                  <div className="eyebrow"><span className="eyebrow-line" /> YOUR COURSE</div>
                  <h1>{selectedCourse.name}</h1>
                  <p>{selectedCourse.description || 'A focused space for your notes and ideas.'}</p>
                </div>
                <div className="course-stats">
                  <div className="stat-icon"><FileText size={17} /></div>
                  <div><strong>{readyDocuments}</strong><span>ready {readyDocuments === 1 ? 'document' : 'documents'}</span></div>
                  <span className="stats-divider" />
                  <div className="stat-icon muted"><Target size={17} /></div>
                  <div><strong>{progress?.quizzesTaken ?? '—'}</strong><span>quizzes taken</span></div>
                </div>
              </section>

              <nav className="tabs" aria-label="Course sections">
                <button className={tab === 'learn' ? 'selected' : ''} onClick={() => setTab('learn')}><MessageSquare size={16} /> Learn</button>
                <button className={tab === 'quiz' ? 'selected' : ''} onClick={() => setTab('quiz')}><Layers3 size={16} /> Practice</button>
                <button className={tab === 'progress' ? 'selected' : ''} onClick={() => setTab('progress')}><GraduationCap size={17} /> Progress</button>
              </nav>

              {tab === 'learn' && <div className="learn-grid">
                <section className="panel chat-panel">
                  <div className="panel-header"><div className="panel-title-icon chat-title-icon"><MessageSquare size={17} /></div><div><h2>Ask your notes</h2><p>Answers are grounded in your uploaded material.</p></div><span className="ai-badge"><span /> AI tutor</span></div>
                  {readyDocuments === 0 ? <div className="chat-empty-state"><div className="empty-icon"><BookOpen size={22} /></div><h3>Start with your study material</h3><p>Upload a PDF or text file. Once it’s ready, ask questions and get answers with page references.</p></div> : <>
                    {chat.length === 0 && <div className="suggestions"><span className="suggestion-label">TRY ASKING</span><button onClick={() => setQuestion('Summarize the main ideas in these notes')}>Summarize the main ideas <ArrowRight size={13} /></button><button onClick={() => setQuestion('Explain the most important concept in simple terms')}>Explain a key concept simply <ArrowRight size={13} /></button></div>}
                    <div className="chat-messages" aria-live="polite">
                      {chat.map((entry, index) => <div key={`${index}-${entry.role}`} className={`message-row ${entry.role}`}>
                        <div className={`message-avatar ${entry.role}`}>{entry.role === 'assistant' ? <Sparkles size={14} /> : 'A'}</div>
                        <div className="message-body"><div className="message-author">{entry.role === 'assistant' ? 'Study tutor' : 'You'}</div><p>{entry.text}</p>
                          {!!entry.sources?.length && <div className="source-list"><div className="source-caption">SOURCES FROM YOUR NOTES</div>{entry.sources.map((source, sourceIndex) => <button className="source-chip" key={`${source.documentId}-${sourceIndex}`} onClick={() => setActiveSource(source)}><FileText size={13} /><span>{source.documentName}{source.page ? ` · p. ${source.page}` : ''}</span><ArrowRight size={12} /></button>)}</div>}
                        </div>
                      </div>)}
                      {asking && <div className="message-row assistant"><div className="message-avatar assistant"><Sparkles size={14} /></div><div className="typing-indicator"><i /><i /><i /></div></div>}
                      <div id="chat-end" />
                    </div>
                  </>}
                  {activeSource && <div className="source-detail"><div><FileText size={15} /><strong>{activeSource.documentName}{activeSource.page ? ` · Page ${activeSource.page}` : ''}</strong></div><p>{activeSource.excerpt}</p><button aria-label="Close source" onClick={() => setActiveSource(null)}><X size={14} /></button></div>}
                  <form className="chat-composer" onSubmit={handleAsk}><input aria-label="Ask a question about your notes" value={question} onChange={(event) => setQuestion(event.target.value)} placeholder={readyDocuments ? 'Ask a question about your notes…' : 'Upload notes to start asking'} disabled={!readyDocuments || asking} /><button aria-label="Send question" disabled={!question.trim() || !readyDocuments || asking}>{asking ? <LoaderCircle className="spin" size={17} /> : <Send size={16} />}</button></form>
                  <div className="privacy-note">Answers are generated from this course’s documents.</div>
                </section>

                <aside className="right-rail">
                  <section className="panel material-panel">
                    <div className="panel-header compact"><div className="panel-title-icon material-title-icon"><BookMarked size={16} /></div><div><h2>Course material</h2><p>{documents.length} {documents.length === 1 ? 'file' : 'files'} added</p></div></div>
                    <div className="document-list">
                      {documents.map((document) => <div className="document-row" key={document.id}><div className="document-icon"><FileText size={15} /></div><div className="document-copy"><strong title={document.fileName}>{document.fileName}</strong><span>{document.status === 'READY' ? 'Ready to study' : document.status === 'FAILED' ? document.errorMessage || 'Could not process file' : 'Reading your notes…'}</span></div><div className={`document-status ${document.status.toLowerCase()}`} title={document.status}>{document.status === 'READY' ? <Check size={14} /> : document.status === 'FAILED' ? <AlertCircle size={14} /> : <LoaderCircle className="spin" size={14} />}</div></div>)}
                      {!documents.length && <p className="no-documents">No material added yet.</p>}
                    </div>
                    <form className="upload-box" onSubmit={handleUpload}>
                      <input ref={fileRef} className="visually-hidden" id="file-upload" type="file" accept=".pdf,.txt,application/pdf,text/plain" onChange={(event) => setSelectedFileName(event.target.files?.[0]?.name ?? '')} />
                      <label htmlFor="file-upload"><span className="upload-icon"><Upload size={16} /></span><strong>{selectedFileName || 'Add course material'}</strong><span>PDF or TXT · up to 10 MB</span></label>
                      <button className="upload-action" disabled={uploading || !selectedFileName} type="submit" title="Upload selected file">{uploading ? <LoaderCircle className="spin" size={15} /> : 'Upload'}</button>
                    </form>
                    <div className="upload-hint"><Clock3 size={13} /> Files are processed in the background</div>
                  </section>
                  <section className="practice-card"><div className="practice-orb"><Sparkles size={17} /></div><div className="eyebrow">READY WHEN YOU ARE</div><h3>Make it stick.</h3><p>Turn your notes into a quick practice quiz and find the topics worth another look.</p><button onClick={() => setTab('quiz')}>Go to practice <ArrowRight size={14} /></button><div className="practice-decoration">✳</div></section>
                </aside>
              </div>}

              {tab === 'quiz' && <section className="panel practice-panel">
                <div className="panel-header"><div className="panel-title-icon quiz-title-icon"><Layers3 size={17} /></div><div><h2>Practice what you’ve learned</h2><p>Build a five-question quiz from your course material.</p></div></div>
                {!quiz && <form className="quiz-start" onSubmit={handleCreateQuiz}><div className="quiz-start-graphic"><div className="quiz-spark"><Sparkles size={18} /></div><div className="quiz-card-mini"><span /><span /><span /></div></div><div className="quiz-start-copy"><h3>A little practice goes a long way.</h3><p>Your quiz will use the notes in this course. Correct answers stay hidden until you submit.</p><label>Focus on a topic <span className="optional">Optional</span><input value={topic} maxLength={160} onChange={(event) => setTopic(event.target.value)} placeholder="e.g. Gradient descent" /></label><button className="primary-button" disabled={creatingQuiz || readyDocuments === 0}>{creatingQuiz ? <><LoaderCircle className="spin" size={16} /> Creating quiz…</> : <><Sparkles size={16} /> Create a quiz <ArrowRight size={16} /></>}</button>{readyDocuments === 0 && <span className="inline-hint">Add a document before creating a quiz.</span>}{quizError && <div className="form-error"><AlertCircle size={15} />{quizError}</div>}</div></form>}
                {quiz && !submission && <div className="quiz-content"><div className="quiz-topline"><div><span className="eyebrow">{quiz.topic || 'COURSE REVIEW'}</span><h3>{quiz.title}</h3></div><span className="question-count">{Object.keys(answers).length} / {quiz.questions.length} answered</span></div><div className="question-list">{quiz.questions.map((item, questionIndex) => <article className="question-card" key={item.id}><div className="question-meta"><span>QUESTION {String(questionIndex + 1).padStart(2, '0')}</span><span>{item.topic}</span></div><h4>{item.prompt}</h4><div className="option-list">{item.options.map((option, optionIndex) => <button key={optionIndex} className={`option-button ${answers[item.id] === optionIndex ? 'chosen' : ''}`} onClick={() => setAnswers((current) => ({ ...current, [item.id]: optionIndex }))}><span className="option-letter">{String.fromCharCode(65 + optionIndex)}</span><span>{option}</span>{answers[item.id] === optionIndex && <Check size={15} />}</button>)}</div></article>)}</div>{quizError && <div className="form-error"><AlertCircle size={15} />{quizError}</div>}<div className="quiz-footer"><button className="text-button" onClick={() => { setQuiz(null); setSubmission(null); setAnswers({}) }}><ArrowLeft size={15} /> Back</button><button className="primary-button" onClick={() => void handleSubmitQuiz()} disabled={submittingQuiz}>{submittingQuiz ? <><LoaderCircle className="spin" size={16} /> Checking…</> : <>Check my answers <ArrowRight size={16} /></>}</button></div></div>}
                {submission && quiz && <div className="results-view"><div className="score-circle"><span>{Math.round(submission.score / submission.totalQuestions * 100)}%</span><small>score</small></div><div className="results-heading"><div className="eyebrow">QUIZ COMPLETE</div><h3>{submission.score === submission.totalQuestions ? 'Beautifully done.' : submission.score >= 3 ? 'You’re getting there.' : 'A good place to learn.'}</h3><p>You got {submission.score} of {submission.totalQuestions} correct. Review the explanations and revisit any tricky topics.</p></div><div className="result-review">{submission.results.map((result, index) => { const item = quiz.questions.find((questionItem) => questionItem.id === result.questionId); return <article className={`result-item ${result.correct ? 'correct' : 'incorrect'}`} key={result.questionId}><div className="result-icon">{result.correct ? <Check size={14} /> : <X size={14} />}</div><div><div className="question-meta"><span>QUESTION {String(index + 1).padStart(2, '0')}</span><span>{result.topic}</span></div><h4>{item?.prompt}</h4>{!result.correct && <p className="correct-answer">Answer: {item?.options[result.correctIndex]}</p>}<p className="explanation">{result.explanation}</p></div></article>})}</div><div className="quiz-footer"><button className="text-button" onClick={() => { setQuiz(null); setSubmission(null); setAnswers({}); setQuizError('') }}><RotateCcwIcon /> New quiz</button><button className="primary-button" onClick={() => setTab('progress')}>View progress <ArrowRight size={16} /></button></div></div>}
              </section>}

              {tab === 'progress' && <section className="progress-layout">
                <div className="progress-intro"><div className="eyebrow"><span className="eyebrow-line" /> YOUR LEARNING JOURNEY</div><h2>Progress, one idea at a time.</h2><p>Every quiz is a small signal about what’s clicking and what could use another look.</p></div>
                <div className="progress-metrics"><article className="metric-card"><span className="metric-icon purple"><Layers3 size={17} /></span><strong>{progress?.quizzesTaken ?? '—'}</strong><span>Quizzes completed</span></article><article className="metric-card"><span className="metric-icon green"><Target size={17} /></span><strong>{progress ? `${progress.averageScorePercent}%` : '—'}</strong><span>Overall accuracy</span></article><article className="metric-card"><span className="metric-icon amber"><BookMarked size={17} /></span><strong>{progress?.topics.length ?? '—'}</strong><span>Topics practiced</span></article></div>
                <div className="progress-columns"><section className="panel topic-panel"><div className="panel-header compact"><div className="panel-title-icon material-title-icon"><Target size={16} /></div><div><h2>Topic confidence</h2><p>Accuracy across your quiz attempts</p></div></div>{progress?.topics.length ? <div className="topic-list">{progress.topics.map((item) => <div className="topic-row" key={item.topic}><div className="topic-row-head"><strong>{item.topic}</strong><span>{Math.round(item.accuracy * 100)}% <small>({item.correct}/{item.total})</small></span></div><div className="progress-track"><span style={{ width: `${Math.round(item.accuracy * 100)}%` }} /></div></div>)}</div> : <div className="progress-empty"><div className="empty-icon"><GraduationCap size={21} /></div><h3>Your learning will show up here.</h3><p>Complete a practice quiz to start building your progress view.</p><button className="text-button" onClick={() => setTab('quiz')}>Try a quiz <ArrowRight size={14} /></button></div>}</section>
                  <aside className="panel review-panel"><div className="panel-header compact"><div className="panel-title-icon review-title-icon"><Sparkles size={16} /></div><div><h2>Worth another look</h2><p>A little review can make a difference.</p></div></div>{progress?.reviewSuggestions.length ? <div className="review-list">{progress.reviewSuggestions.map((suggestion) => <div className="review-item" key={suggestion}><span className="review-dot" /><span>{suggestion}</span><ArrowRight size={14} /></div>)}</div> : <div className="review-empty"><CheckCircle2 size={20} /><p>{progress?.quizzesTaken ? 'No topics need extra review yet. Keep going!' : 'After a few quizzes, we’ll suggest topics to revisit.'}</p></div>}</aside></div>
              </section>}
            </>
          ) : loadingCourses ? <div className="loading-page"><LoaderCircle className="spin" /> Loading your study space…</div> : <div className="loading-page">Choose a course to continue.</div>}
          <footer className="page-footer"><span>Made for focused learning</span><span><span className="footer-dot" /> Your study space</span></footer>
        </div>
      </main>
    </div>
  )
}

function RotateCcwIcon() { return <ArrowLeft size={15} /> }

export default App
