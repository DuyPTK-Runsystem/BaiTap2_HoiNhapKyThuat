import { BrandMark } from '../components/BrandMark'
import type { ReactNode } from 'react'

interface AuthLayoutProps {
  title: string
  subtitle?: string
  children: ReactNode
}

export function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <main className="auth-layout">
      <section className="auth-panel">
        <div className="app-brand">
          <BrandMark />
          <strong>Vocab Library</strong>
        </div>
        <h1>{title}</h1>
        {subtitle ? <p className="auth-subtitle">{subtitle}</p> : null}
        {children}
      </section>
    </main>
  )
}
