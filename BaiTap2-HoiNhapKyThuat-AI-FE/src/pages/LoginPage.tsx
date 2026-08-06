import { useState } from 'react'
import { AuthLayout } from '../layouts/AuthLayout'
import type { AuthCredentials } from '../types/auth'
import type { FormEvent } from 'react'

interface LoginPageProps {
  submitting: boolean
  errorMessage: string | null
  successMessage: string | null
  onSubmit: (credentials: AuthCredentials) => Promise<void>
  onSwitchToRegister: () => void
}

export function LoginPage({
  submitting,
  errorMessage,
  successMessage,
  onSubmit,
  onSwitchToRegister,
}: LoginPageProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (submitting) {
      return
    }

    await onSubmit({ email, password })
  }

  return (
    <AuthLayout title="Login Page" subtitle="Đăng nhập để quản lý thư viện từ vựng và phiên học của bạn.">
      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          <span className="field-label">
            Email <span className="required-mark">(*)</span>
          </span>
          <input
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            type="email"
            placeholder="Nhập email của bạn"
            required
            disabled={submitting}
          />
        </label>

        <label>
          <span className="field-label">
            Mật khẩu <span className="required-mark">(*)</span>
          </span>
          <input
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            placeholder="Nhập mật khẩu của bạn"
            required
            disabled={submitting}
          />
        </label>

        {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
        {successMessage ? <p className="form-success">{successMessage}</p> : null}

        <button type="submit" disabled={submitting}>
          {submitting ? 'Đang đăng nhập' : 'Đăng nhập'}
        </button>

        <p className="auth-switch">
          <span>Chưa có tài khoản?</span>
          <button type="button" onClick={onSwitchToRegister} disabled={submitting}>
            Đăng ký
          </button>
        </p>
      </form>
    </AuthLayout>
  )
}
