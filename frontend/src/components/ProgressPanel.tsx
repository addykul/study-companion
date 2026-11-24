import { ArrowRight, BookMarked, CheckCircle2, GraduationCap, Layers3, Sparkles, Target } from 'lucide-react'
import type { Progress } from '../api'

export function ProgressPanel({ progress, onPractice }: { progress: Progress | null; onPractice: () => void }) {
  return (
    <section className="progress-layout">
      <div className="progress-intro"><div className="eyebrow"><span className="eyebrow-line" /> YOUR LEARNING JOURNEY</div><h2>Progress, one idea at a time.</h2><p>Every quiz is a small signal about what’s clicking and what could use another look.</p></div>
      <div className="progress-metrics">
        <article className="metric-card"><span className="metric-icon purple"><Layers3 size={17} /></span><strong>{progress?.quizzesTaken ?? '—'}</strong><span>Quizzes completed</span></article>
        <article className="metric-card"><span className="metric-icon green"><Target size={17} /></span><strong>{progress ? `${progress.averageScorePercent}%` : '—'}</strong><span>Overall accuracy</span></article>
        <article className="metric-card"><span className="metric-icon amber"><BookMarked size={17} /></span><strong>{progress?.topics.length ?? '—'}</strong><span>Topics practiced</span></article>
      </div>
      <div className="progress-columns">
        <section className="panel topic-panel">
          <div className="panel-header compact"><div className="panel-title-icon material-title-icon"><Target size={16} /></div><div><h2>Topic confidence</h2><p>Accuracy across your quiz attempts</p></div></div>
          {progress?.topics.length ? <div className="topic-list">{progress.topics.map((item) => <div className="topic-row" key={item.topic}><div className="topic-row-head"><strong>{item.topic}</strong><span>{Math.round(item.accuracy * 100)}% <small>({item.correct}/{item.total})</small></span></div><div className="progress-track"><span style={{ width: `${Math.round(item.accuracy * 100)}%` }} /></div></div>)}</div> : <div className="progress-empty"><div className="empty-icon"><GraduationCap size={21} /></div><h3>Your learning will show up here.</h3><p>Complete a practice quiz to start building your progress view.</p><button className="text-button" onClick={onPractice}>Try a quiz <ArrowRight size={14} /></button></div>}
        </section>
        <aside className="panel review-panel">
          <div className="panel-header compact"><div className="panel-title-icon review-title-icon"><Sparkles size={16} /></div><div><h2>Worth another look</h2><p>A little review can make a difference.</p></div></div>
          {progress?.reviewSuggestions.length ? <div className="review-list">{progress.reviewSuggestions.map((suggestion) => <div className="review-item" key={suggestion}><span className="review-dot" /><span>{suggestion}</span><ArrowRight size={14} /></div>)}</div> : <div className="review-empty"><CheckCircle2 size={20} /><p>{progress?.quizzesTaken ? 'No topics need extra review yet. Keep going!' : 'After a few quizzes, we’ll suggest topics to revisit.'}</p></div>}
        </aside>
      </div>
    </section>
  )
}
