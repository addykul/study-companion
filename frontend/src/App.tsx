import { AlertCircle, LoaderCircle, X } from 'lucide-react'
import { ChatPanel } from './components/ChatPanel'
import { CreateCourseDialog } from './components/CreateCourseDialog'
import { MaterialsPanel } from './components/MaterialsPanel'
import { ProgressPanel } from './components/ProgressPanel'
import { QuizPanel } from './components/QuizPanel'
import { Sidebar } from './components/Sidebar'
import { WelcomePanel } from './components/WelcomePanel'
import { WorkspaceHeader } from './components/WorkspaceHeader'
import { useStudyCompanion } from './hooks/useStudyCompanion'

function App() {
  const workspace = useStudyCompanion()
  const {
    courses, selectedId, selectedCourse, documents, readyDocuments, progress, tab,
    loadingCourses, pageError, setPageError, showCreate, setShowCreate,
    newName, setNewName, newDescription, setNewDescription, creating,
    uploading, selectedFileName, setSelectedFileName, question, setQuestion,
    asking, loadingChat, chat, activeSource, setActiveSource, topic, setTopic, quiz, answers,
    setAnswers, submission, creatingQuiz, submittingQuiz, quizError,
    setTab, selectCourse, handleCreateCourse, handleUpload, handleAsk,
    handleCreateQuiz, handleSubmitQuiz, resetQuiz,
  } = workspace

  return (
    <div className="app-shell">
      <Sidebar courses={courses} selectedId={selectedId} loading={loadingCourses} onSelect={selectCourse} onCreate={() => setShowCreate(true)} />

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb"><span>Workspace</span><span className="crumb-slash">/</span><strong>{selectedCourse?.name ?? 'My learning'}</strong></div>
          <div className="topbar-right"><span className="connection-dot" /> <span>Personal study space</span><span className="avatar">A</span></div>
        </header>

        <div className="content-wrap">
          {pageError && <div className="notice error-notice" role="alert"><AlertCircle size={16} /><span>{pageError}</span><button aria-label="Dismiss message" onClick={() => setPageError('')}><X size={15} /></button></div>}

          <CreateCourseDialog
            open={showCreate}
            name={newName}
            description={newDescription}
            creating={creating}
            onNameChange={setNewName}
            onDescriptionChange={setNewDescription}
            onClose={() => setShowCreate(false)}
            onSubmit={handleCreateCourse}
          />

          {!loadingCourses && courses.length === 0 ? (
            <WelcomePanel onCreate={() => setShowCreate(true)} />
          ) : selectedCourse ? (
            <>
              <WorkspaceHeader course={selectedCourse} readyDocuments={readyDocuments} progress={progress} tab={tab} onTabChange={setTab} />
              {tab === 'learn' && <div className="learn-grid">
                <ChatPanel
                  readyDocuments={readyDocuments}
                  chat={chat}
                  question={question}
                  asking={asking}
                  loadingChat={loadingChat}
                  activeSource={activeSource}
                  onQuestionChange={setQuestion}
                  onAsk={handleAsk}
                  onSelectSource={setActiveSource}
                  onCloseSource={() => setActiveSource(null)}
                />
                <MaterialsPanel
                  documents={documents}
                  uploading={uploading}
                  selectedFileName={selectedFileName}
                  onSelectedFileChange={setSelectedFileName}
                  onUpload={handleUpload}
                  onTabChange={setTab}
                />
              </div>}
              {tab === 'quiz' && <QuizPanel
                readyDocuments={readyDocuments}
                topic={topic}
                quiz={quiz}
                answers={answers}
                submission={submission}
                creatingQuiz={creatingQuiz}
                submittingQuiz={submittingQuiz}
                error={quizError}
                onTopicChange={setTopic}
                onCreate={handleCreateQuiz}
                onAnswer={(questionId, optionIndex) => setAnswers((current) => ({ ...current, [questionId]: optionIndex }))}
                onSubmit={() => { void handleSubmitQuiz() }}
                onReset={resetQuiz}
                onViewProgress={() => setTab('progress')}
              />}
              {tab === 'progress' && <ProgressPanel progress={progress} onPractice={() => setTab('quiz')} />}
            </>
          ) : loadingCourses ? (
            <div className="loading-page"><LoaderCircle className="spin" /> Loading your study space…</div>
          ) : (
            <div className="loading-page">Choose a course to continue.</div>
          )}
          <footer className="page-footer"><span>Made for focused learning</span><span><span className="footer-dot" /> Your study space</span></footer>
        </div>
      </main>
    </div>
  )
}

export default App
