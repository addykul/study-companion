import { useRef, type FormEvent } from 'react'
import { AlertCircle, ArrowRight, BookMarked, Check, Clock3, FileText, LoaderCircle, Sparkles, Upload } from 'lucide-react'
import type { CourseDocument } from '../api'
import type { StudyTab } from '../hooks/useStudyCompanion'

type MaterialsPanelProps = {
  documents: CourseDocument[]
  uploading: boolean
  selectedFileName: string
  onSelectedFileChange: (name: string) => void
  onUpload: (event: FormEvent<HTMLFormElement>, file: File | undefined, input: HTMLInputElement | null) => void
  onTabChange: (tab: StudyTab) => void
}

export function MaterialsPanel({ documents, uploading, selectedFileName, onSelectedFileChange, onUpload, onTabChange }: MaterialsPanelProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  function submitUpload(event: FormEvent<HTMLFormElement>) {
    onUpload(event, fileInputRef.current?.files?.[0], fileInputRef.current)
  }

  return (
    <aside className="right-rail">
      <section className="panel material-panel">
        <div className="panel-header compact"><div className="panel-title-icon material-title-icon"><BookMarked size={16} /></div><div><h2>Course material</h2><p>{documents.length} {documents.length === 1 ? 'file' : 'files'} added</p></div></div>
        <div className="document-list">
          {documents.map((document) => <div className="document-row" key={document.id}>
            <div className="document-icon"><FileText size={15} /></div>
            <div className="document-copy"><strong title={document.fileName}>{document.fileName}</strong><span>{document.status === 'READY' ? 'Ready to study' : document.status === 'FAILED' ? document.errorMessage || 'Could not process file' : 'Reading your notes…'}</span></div>
            <div className={`document-status ${document.status.toLowerCase()}`} title={document.status} aria-label={`Document status: ${document.status}`}>
              {document.status === 'READY' ? <Check size={14} /> : document.status === 'FAILED' ? <AlertCircle size={14} /> : <LoaderCircle className="spin" size={14} />}
            </div>
          </div>)}
          {!documents.length && <p className="no-documents">No material added yet.</p>}
        </div>
        <form className="upload-box" onSubmit={submitUpload}>
          <input ref={fileInputRef} className="visually-hidden" id="file-upload" type="file" accept=".pdf,.txt,application/pdf,text/plain" onChange={(event) => onSelectedFileChange(event.target.files?.[0]?.name ?? '')} />
          <label htmlFor="file-upload"><span className="upload-icon"><Upload size={16} /></span><strong>{selectedFileName || 'Add course material'}</strong><span>PDF or TXT · up to 10 MB</span></label>
          <button className="upload-action" disabled={uploading || !selectedFileName} type="submit" title="Upload selected file">{uploading ? <LoaderCircle className="spin" size={15} /> : 'Upload'}</button>
        </form>
        <div className="upload-hint"><Clock3 size={13} /> Files are processed in the background</div>
      </section>
      <section className="practice-card"><div className="practice-orb"><Sparkles size={17} /></div><div className="eyebrow">READY WHEN YOU ARE</div><h3>Make it stick.</h3><p>Turn your notes into a quick practice quiz and find the topics worth another look.</p><button onClick={() => onTabChange('quiz')}>Go to practice <ArrowRight size={14} /></button><div className="practice-decoration">✳</div></section>
    </aside>
  )
}
