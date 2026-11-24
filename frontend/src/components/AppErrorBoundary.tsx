import { Component, type ErrorInfo, type ReactNode } from 'react'
import { AlertCircle } from 'lucide-react'

type AppErrorBoundaryProps = { children: ReactNode }
type AppErrorBoundaryState = { hasError: boolean }

export class AppErrorBoundary extends Component<AppErrorBoundaryProps, AppErrorBoundaryState> {
  state: AppErrorBoundaryState = { hasError: false }

  static getDerivedStateFromError(): AppErrorBoundaryState {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Study Companion UI error', error, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      return <main className="fatal-error" role="alert"><AlertCircle size={22} /><h1>Something went wrong</h1><p>Reload the page to continue using Study Companion.</p><button className="primary-button" onClick={() => window.location.reload()}>Reload app</button></main>
    }
    return this.props.children
  }
}
