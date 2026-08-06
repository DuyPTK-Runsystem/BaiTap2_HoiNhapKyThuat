import './App.css'
import { AuthGate } from './components/AuthGate'
import { AppLayout } from './layouts/AppLayout'
import { FlashcardSessionPage } from './pages/FlashcardSessionPage'
import { LibraryPage } from './pages/LibraryPage'
import { LibrarySearchPage } from './pages/LibrarySearchPage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { RegisterPage } from './pages/RegisterPage'
import { StudyCreatePage } from './pages/StudyCreatePage'
import { TestResultPage } from './pages/TestResultPage'
import { TestReviewPage } from './pages/TestReviewPage'
import { TestTakingPage } from './pages/TestTakingPage'
import { useAuthSession } from './hooks/useAuthSession'
import { BrowserRouter, Navigate, Outlet, Route, Routes, useNavigate } from 'react-router-dom'
import type { AuthSession } from './hooks/useAuthSession'

interface AuthRouteProps {
  session: AuthSession
}

function LoginRoute({ session }: AuthRouteProps) {
  const navigate = useNavigate()

  return (
    <LoginPage
      submitting={session.submitting}
      errorMessage={session.errorMessage}
      successMessage={session.successMessage}
      onSubmit={session.signIn}
      onSwitchToRegister={() => {
        session.clearError()
        navigate('/register')
      }}
    />
  )
}

function RegisterRoute({ session }: AuthRouteProps) {
  const navigate = useNavigate()

  return (
    <RegisterPage
      submitting={session.submitting}
      errorMessage={session.errorMessage}
      successMessage={session.successMessage}
      onSubmit={session.signUp}
      onSwitchToLogin={() => {
        session.clearError()
        navigate('/login')
      }}
    />
  )
}

function RootRedirect({ session }: AuthRouteProps) {
  if (session.isChecking) {
    return (
      <AuthGate session={session} access="protected">
        <Outlet />
      </AuthGate>
    )
  }

  return <Navigate to={session.isAuthenticated ? '/library' : '/login'} replace />
}

function ProtectedShell({ session }: AuthRouteProps) {
  return (
    <AuthGate session={session} access="protected">
      <AppLayout user={session.user} submitting={session.submitting} onLogout={session.signOut}>
        <Outlet />
      </AppLayout>
    </AuthGate>
  )
}

function App() {
  const authSession = useAuthSession()

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<RootRedirect session={authSession} />} />
        <Route
          path="/login"
          element={
            <AuthGate session={authSession} access="guest">
              <LoginRoute session={authSession} />
            </AuthGate>
          }
        />
        <Route
          path="/register"
          element={
            <AuthGate session={authSession} access="guest">
              <RegisterRoute session={authSession} />
            </AuthGate>
          }
        />

        <Route element={<ProtectedShell session={authSession} />}>
          <Route path="/library" element={<LibraryPage />} />
          <Route path="/library/search" element={<LibrarySearchPage />} />
          <Route path="/study/create" element={<StudyCreatePage />} />
          <Route path="/tests/:testId" element={<TestTakingPage />} />
          <Route path="/tests/:testId/result" element={<TestResultPage />} />
          <Route path="/tests/:testId/review" element={<TestReviewPage />} />
          <Route path="/flashcards/session" element={<FlashcardSessionPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
