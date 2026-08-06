import { StatusMessage } from './StatusMessage'
import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import type { AuthSession } from '../hooks/useAuthSession'

interface AuthGateProps {
  session: AuthSession
  access: 'guest' | 'protected'
  children: ReactNode
}

export function AuthGate({ session, access, children }: AuthGateProps) {
  const location = useLocation()

  if (session.isChecking) {
    return (
      <main className="auth-layout">
        <StatusMessage tone="loading" title="Đang kiểm tra phiên đăng nhập" message="Vui lòng chờ trong giây lát." />
      </main>
    )
  }

  if (access === 'protected' && !session.isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (access === 'guest' && session.isAuthenticated) {
    return <Navigate to="/library" replace />
  }

  return <>{children}</>
}
