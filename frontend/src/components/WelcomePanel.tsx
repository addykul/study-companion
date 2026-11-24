import { ArrowRight, CheckCircle2, Plus, Sparkles } from 'lucide-react'

export function WelcomePanel({ onCreate }: { onCreate: () => void }) {
  return (
    <section className="welcome-card">
      <div className="welcome-illustration"><div className="sun-shape" /><div className="book-stack"><span /><span /><span /></div><Sparkles className="welcome-spark spark-one" size={18} /><Sparkles className="welcome-spark spark-two" size={13} /></div>
      <div className="eyebrow">A calmer way to study</div>
      <h1>Your notes, ready to<br /><em>teach you back.</em></h1>
      <p>Create a course, add your study material, and ask questions grounded in your own notes. Then practice what you’ve learned with a quick quiz.</p>
      <button className="primary-button" onClick={onCreate}><Plus size={17} /> Create your first course <ArrowRight size={16} /></button>
      <div className="welcome-footnote"><CheckCircle2 size={15} /> Files are stored locally; retrieved excerpts go to your configured AI provider.</div>
    </section>
  )
}
