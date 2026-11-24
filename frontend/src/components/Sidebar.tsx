import { BookOpen, ChevronDown, LoaderCircle, Plus, Sparkles } from 'lucide-react'
import type { Course } from '../api'

type SidebarProps = {
  courses: Course[]
  selectedId: string | null
  loading: boolean
  onSelect: (courseId: string) => void
  onCreate: () => void
}

export function Sidebar({ courses, selectedId, loading, onSelect, onCreate }: SidebarProps) {
  return (
    <aside className="sidebar">
      <a className="brand" href="#home" onClick={(event) => event.preventDefault()}>
        <span className="brand-mark"><BookOpen size={19} strokeWidth={2.2} /></span>
        <span>study<span className="brand-light">space</span></span>
      </a>
      <div className="sidebar-section-label">YOUR LIBRARY <span className="library-count">{courses.length}</span></div>
      <div className="course-list">
        {loading && <div className="sidebar-loading"><LoaderCircle className="spin" size={15} /> Loading courses</div>}
        {!loading && courses.map((course, index) => (
          <button key={course.id} className={`course-nav ${selectedId === course.id ? 'active' : ''}`} onClick={() => onSelect(course.id)}>
            <span className={`course-dot dot-${index % 4}`} />
            <span className="course-nav-copy"><span className="course-nav-name">{course.name}</span><span className="course-nav-meta">{course.documentCount} {course.documentCount === 1 ? 'document' : 'documents'}</span></span>
            {selectedId === course.id && <ChevronDown size={15} className="nav-chevron" />}
          </button>
        ))}
        {!loading && courses.length === 0 && <p className="empty-library">Your courses will appear here.</p>}
      </div>
      <button className="new-course-button" onClick={onCreate}><Plus size={16} /> New course</button>
      <div className="sidebar-bottom">
        <div className="sidebar-tip-icon"><Sparkles size={15} /></div>
        <p>Small steps add up.<br /><span>Keep a little momentum today.</span></p>
      </div>
    </aside>
  )
}
