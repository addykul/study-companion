import { useEffect, useRef, type FormEvent } from 'react'
import { AlertCircle, ArrowRight, BookOpen, FileText, LoaderCircle, MessageSquare, Send, Sparkles, X } from 'lucide-react'
import type { Source } from '../api'
import type { ChatEntry } from '../hooks/useStudyCompanion'

type ChatPanelProps = {
  readyDocuments: number
  chat: ChatEntry[]
  question: string
  asking: boolean
  activeSource: Source | null
  onQuestionChange: (value: string) => void
  onAsk: (event: FormEvent<HTMLFormElement>) => void
  onSelectSource: (source: Source) => void
  onCloseSource: () => void
}

const SUGGESTED_QUESTIONS = [
  'Summarize the main ideas in these notes',
  'Explain the most important concept in simple terms',
]

export function ChatPanel(props: ChatPanelProps) {
  const { readyDocuments, chat, question, asking, activeSource, onQuestionChange, onAsk, onSelectSource, onCloseSource } = props
  const endOfChatRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    endOfChatRef.current?.scrollIntoView?.({ behavior: 'smooth', block: 'end' })
  }, [chat, asking])

  return (
    <section className="panel chat-panel">
      <div className="panel-header"><div className="panel-title-icon chat-title-icon"><MessageSquare size={17} /></div><div><h2>Ask your notes</h2><p>Answers are grounded in your uploaded material.</p></div><span className="ai-badge"><span /> AI tutor</span></div>
      {readyDocuments === 0 ? (
        <div className="chat-empty-state"><div className="empty-icon"><BookOpen size={22} /></div><h3>Start with your study material</h3><p>Upload a PDF or text file. Once it’s ready, ask questions and get answers with page references.</p></div>
      ) : <>
        {chat.length === 0 && <div className="suggestions"><span className="suggestion-label">TRY ASKING</span>{SUGGESTED_QUESTIONS.map((suggestion) => <button key={suggestion} onClick={() => onQuestionChange(suggestion)}>{suggestion.startsWith('Summarize') ? 'Summarize the main ideas' : 'Explain a key concept simply'} <ArrowRight size={13} /></button>)}</div>}
        <div className="chat-messages" aria-live="polite" aria-relevant="additions text">
          {chat.map((entry) => <div key={entry.id} className={`message-row ${entry.role}`}>
            <div className={`message-avatar ${entry.role}`}>{entry.role === 'assistant' ? <Sparkles size={14} /> : 'A'}</div>
            <div className="message-body"><div className="message-author">{entry.role === 'assistant' ? 'Study tutor' : 'You'}</div><p>{entry.text}</p>
              {!!entry.sources?.length && <div className="source-list"><div className="source-caption">SOURCES FROM YOUR NOTES</div>{entry.sources.map((source, index) => <button className="source-chip" key={`${source.documentId}-${index}`} onClick={() => onSelectSource(source)}><FileText size={13} /><span>{source.documentName}{source.page ? ` · p. ${source.page}` : ''}</span><ArrowRight size={12} /></button>)}</div>}
            </div>
          </div>)}
          {asking && <div className="message-row assistant" aria-label="Study tutor is responding"><div className="message-avatar assistant"><Sparkles size={14} /></div><div className="typing-indicator"><i /><i /><i /></div></div>}
          <div ref={endOfChatRef} />
        </div>
      </>}
      {activeSource && <div className="source-detail"><div><FileText size={15} /><strong>{activeSource.documentName}{activeSource.page ? ` · Page ${activeSource.page}` : ''}</strong></div><p>{activeSource.excerpt}</p><button aria-label="Close source" onClick={onCloseSource}><X size={14} /></button></div>}
      <form className="chat-composer" onSubmit={onAsk}><input aria-label="Ask a question about your notes" value={question} onChange={(event) => onQuestionChange(event.target.value)} placeholder={readyDocuments ? 'Ask a question about your notes…' : 'Upload notes to start asking'} disabled={!readyDocuments || asking} /><button aria-label="Send question" disabled={!question.trim() || !readyDocuments || asking}>{asking ? <LoaderCircle className="spin" size={17} /> : <Send size={16} />}</button></form>
      <div className="privacy-note">Answers are generated from this course’s documents.</div>
    </section>
  )
}

export function InlineError({ message }: { message: string }) {
  return message ? <div className="form-error" role="alert"><AlertCircle size={15} />{message}</div> : null
}
