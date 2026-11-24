import { useEffect, useRef, type FormEvent } from 'react'
import { BookMarked, LoaderCircle, Plus, X } from 'lucide-react'

type CreateCourseDialogProps = {
  open: boolean
  name: string
  description: string
  creating: boolean
  onNameChange: (value: string) => void
  onDescriptionChange: (value: string) => void
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export function CreateCourseDialog(props: CreateCourseDialogProps) {
  const { open, name, description, creating, onNameChange, onDescriptionChange, onClose, onSubmit } = props
  const dialogRef = useRef<HTMLFormElement>(null)
  const onCloseRef = useRef(onClose)
  const creatingRef = useRef(creating)
  onCloseRef.current = onClose
  creatingRef.current = creating

  useEffect(() => {
    if (!open) return
    const previousFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null
    dialogRef.current?.querySelector<HTMLInputElement>('input')?.focus()
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !creatingRef.current) onCloseRef.current()
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => {
      window.removeEventListener('keydown', handleKeyDown)
      previousFocus?.focus()
    }
  }, [open])

  if (!open) return null

  return (
    <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget && !creating) onClose() }}>
      <form ref={dialogRef} className="create-modal" role="dialog" aria-modal="true" aria-labelledby="create-course-title" onSubmit={onSubmit}>
        <button type="button" className="modal-close" aria-label="Close" onClick={onClose} disabled={creating}><X size={18} /></button>
        <div className="modal-icon"><BookMarked size={20} /></div>
        <h2 id="create-course-title">Create a course</h2>
        <p>Make a dedicated space for notes, questions, and practice.</p>
        <label>Course name<input required maxLength={120} value={name} onChange={(event) => onNameChange(event.target.value)} placeholder="e.g. Introduction to machine learning" /></label>
        <label>Description <span className="optional">Optional</span><textarea maxLength={500} value={description} onChange={(event) => onDescriptionChange(event.target.value)} placeholder="What are you working on?" rows={3} /></label>
        <button className="primary-button modal-submit" disabled={!name.trim() || creating}>{creating ? <LoaderCircle className="spin" size={16} /> : <Plus size={16} />} Create course</button>
      </form>
    </div>
  )
}
