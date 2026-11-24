import type { FormEvent } from 'react'
import { useMemo } from 'react'
import { ArrowLeft, ArrowRight, Check, Layers3, LoaderCircle, Sparkles, X } from 'lucide-react'
import type { Quiz, QuizSubmission } from '../api'
import { InlineError } from './ChatPanel'

type QuizPanelProps = {
  readyDocuments: number
  topic: string
  quiz: Quiz | null
  answers: Record<string, number>
  submission: QuizSubmission | null
  creatingQuiz: boolean
  submittingQuiz: boolean
  error: string
  onTopicChange: (value: string) => void
  onCreate: (event: FormEvent<HTMLFormElement>) => void
  onAnswer: (questionId: string, optionIndex: number) => void
  onSubmit: () => void
  onReset: () => void
  onViewProgress: () => void
}

export function QuizPanel(props: QuizPanelProps) {
  const { readyDocuments, topic, quiz, answers, submission, creatingQuiz, submittingQuiz, error, onTopicChange, onCreate, onAnswer, onSubmit, onReset, onViewProgress } = props
  const questionById = useMemo(() => new Map(quiz?.questions.map((question) => [question.id, question]) ?? []), [quiz])

  return (
    <section className="panel practice-panel">
      <div className="panel-header"><div className="panel-title-icon quiz-title-icon"><Layers3 size={17} /></div><div><h2>Practice what you’ve learned</h2><p>Build a five-question quiz from your course material.</p></div></div>
      {!quiz && <form className="quiz-start" onSubmit={onCreate}>
        <div className="quiz-start-graphic"><div className="quiz-spark"><Sparkles size={18} /></div><div className="quiz-card-mini"><span /><span /><span /></div></div>
        <div className="quiz-start-copy"><h3>A little practice goes a long way.</h3><p>Your quiz will use the notes in this course. Correct answers stay hidden until you submit.</p>
          <label>Focus on a topic <span className="optional">Optional</span><input value={topic} maxLength={160} onChange={(event) => onTopicChange(event.target.value)} placeholder="e.g. Gradient descent" /></label>
          <button className="primary-button" disabled={creatingQuiz || readyDocuments === 0}>{creatingQuiz ? <><LoaderCircle className="spin" size={16} /> Creating quiz…</> : <><Sparkles size={16} /> Create a quiz <ArrowRight size={16} /></>}</button>
          {readyDocuments === 0 && <span className="inline-hint">Add a document before creating a quiz.</span>}
          <InlineError message={error} />
        </div>
      </form>}

      {quiz && !submission && <div className="quiz-content">
        <div className="quiz-topline"><div><span className="eyebrow">{quiz.topic || 'COURSE REVIEW'}</span><h3>{quiz.title}</h3></div><span className="question-count">{Object.keys(answers).length} / {quiz.questions.length} answered</span></div>
        <div className="question-list">{quiz.questions.map((item, questionIndex) => <article className="question-card" key={item.id}>
          <div className="question-meta"><span>QUESTION {String(questionIndex + 1).padStart(2, '0')}</span><span>{item.topic}</span></div><h4>{item.prompt}</h4>
          <div className="option-list">{item.options.map((option, optionIndex) => <button type="button" key={`${item.id}-${optionIndex}`} className={`option-button ${answers[item.id] === optionIndex ? 'chosen' : ''}`} aria-pressed={answers[item.id] === optionIndex} onClick={() => onAnswer(item.id, optionIndex)}><span className="option-letter">{String.fromCharCode(65 + optionIndex)}</span><span>{option}</span>{answers[item.id] === optionIndex && <Check size={15} />}</button>)}</div>
        </article>)}</div>
        <InlineError message={error} />
        <div className="quiz-footer"><button className="text-button" onClick={onReset}><ArrowLeft size={15} /> Back</button><button className="primary-button" onClick={onSubmit} disabled={submittingQuiz}>{submittingQuiz ? <><LoaderCircle className="spin" size={16} /> Checking…</> : <>Check my answers <ArrowRight size={16} /></>}</button></div>
      </div>}

      {submission && quiz && <div className="results-view">
        <div className="score-circle"><span>{submission.totalQuestions ? Math.round(submission.score / submission.totalQuestions * 100) : 0}%</span><small>score</small></div>
        <div className="results-heading"><div className="eyebrow">QUIZ COMPLETE</div><h3>{submission.score === submission.totalQuestions ? 'Beautifully done.' : submission.score >= 3 ? 'You’re getting there.' : 'A good place to learn.'}</h3><p>You got {submission.score} of {submission.totalQuestions} correct. Review the explanations and revisit any tricky topics.</p></div>
        <div className="result-review">{submission.results.map((result, index) => {
          const item = questionById.get(result.questionId)
          return <article className={`result-item ${result.correct ? 'correct' : 'incorrect'}`} key={result.questionId}>
            <div className="result-icon">{result.correct ? <Check size={14} /> : <X size={14} />}</div>
            <div><div className="question-meta"><span>QUESTION {String(index + 1).padStart(2, '0')}</span><span>{result.topic}</span></div><h4>{item?.prompt}</h4>{!result.correct && <p className="correct-answer">Answer: {item?.options[result.correctIndex]}</p>}<p className="explanation">{result.explanation}</p></div>
          </article>
        })}</div>
        <div className="quiz-footer"><button className="text-button" onClick={onReset}><ArrowLeft size={15} /> New quiz</button><button className="primary-button" onClick={onViewProgress}>View progress <ArrowRight size={16} /></button></div>
      </div>}
    </section>
  )
}
