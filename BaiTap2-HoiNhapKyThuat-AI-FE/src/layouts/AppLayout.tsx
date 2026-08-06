import { BrandMark } from '../components/BrandMark'
import { NavLink, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import type { User } from '../types/auth'

interface AppLayoutProps {
  user: User | null
  submitting: boolean
  onLogout: () => void
  children: ReactNode
}

const navigationItems = [
  {
    label: 'Vocabulary Library',
    to: '/library',
    match: ['/library'],
  },
  {
    label: 'Study',
    to: '/study/create',
    match: ['/study', '/tests'],
  },
  {
    label: 'Flashcards',
    to: '/flashcards/session',
    match: ['/flashcards'],
  },
] as const

const routeTitles: Record<string, { eyebrow: string; title: string }> = {
  '/library': { eyebrow: 'Vocabulary', title: 'Library' },
  '/library/search': { eyebrow: 'Vocabulary', title: 'Search' },
  '/study/create': { eyebrow: 'Study', title: 'Create Session' },
  '/flashcards/session': { eyebrow: 'Flashcards', title: 'Session' },
}

export function AppLayout({ user, submitting, onLogout, children }: AppLayoutProps) {
  const location = useLocation()
  const title = routeTitles[location.pathname] ?? {
    eyebrow: location.pathname.startsWith('/tests') ? 'Testing' : 'Workspace',
    title: location.pathname.startsWith('/tests') ? 'Test Session' : 'Workspace',
  }

  return (
    <div className="app-shell">
      <aside className="app-sidebar" aria-label="Điều hướng chính">
        <div className="app-brand">
          <BrandMark />
          <div>
            <strong>Vocab Library</strong>
            <span>English learning support</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {navigationItems.map((item) => (
            <NavLink
              className={({ isActive }) =>
                isActive || item.match.some((path) => location.pathname.startsWith(path)) ? 'is-active' : ''
              }
              to={item.to}
              key={item.to}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <div>
            <p className="eyebrow">{title.eyebrow}</p>
            <h1>{title.title}</h1>
          </div>
          <div className="session-panel">
            <span>{user ? user.email : 'Chưa đăng nhập'}</span>
            <button type="button" onClick={onLogout} disabled={!user || submitting}>
              {submitting ? 'Đang đăng xuất' : 'Đăng xuất'}
            </button>
          </div>
        </header>

        <main className="workspace">{children}</main>
      </div>
    </div>
  )
}
