interface StatusMessageProps {
  tone: 'loading' | 'empty' | 'error' | 'success'
  title: string
  message?: string
}

export function StatusMessage({ tone, title, message }: StatusMessageProps) {
  return (
    <section className={`status-message status-message--${tone}`} aria-live={tone === 'error' ? 'assertive' : 'polite'}>
      <strong>{title}</strong>
      {message ? <span>{message}</span> : null}
    </section>
  )
}
