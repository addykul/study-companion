import { FileText, GraduationCap, Layers3, MessageSquare, Target } from 'lucide-react'
import type { Course, Progress } from '../api'
import type { StudyTab } from '../hooks/useStudyCompanion'

type WorkspaceHeaderProps = {
  course: Course
  readyDocuments: number
  progress: Progress | null
  tab: StudyTab
  onTabChange: (tab: StudyTab) => void
}

export function WorkspaceHeader({ course, readyDocuments, progress, tab, onTabChange }: WorkspaceHeaderProps) {
  return (
    <>
      <section className="course-heading">
        <div className="course-heading-main">
          <div className="eyebrow"><span className="eyebrow-line" /> YOUR COURSE</div>
          <h1>{course.name}</h1>
          <p>{course.description || 'A focused space for your notes and ideas.'}</p>
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
        <button className={tab === 'learn' ? 'selected' : ''} aria-current={tab === 'learn' ? 'page' : undefined} onClick={() => onTabChange('learn')}><MessageSquare size={16} /> Learn</button>
        <button className={tab === 'quiz' ? 'selected' : ''} aria-current={tab === 'quiz' ? 'page' : undefined} onClick={() => onTabChange('quiz')}><Layers3 size={16} /> Practice</button>
        <button className={tab === 'progress' ? 'selected' : ''} aria-current={tab === 'progress' ? 'page' : undefined} onClick={() => onTabChange('progress')}><GraduationCap size={17} /> Progress</button>
      </nav>
    </>
  )
}
