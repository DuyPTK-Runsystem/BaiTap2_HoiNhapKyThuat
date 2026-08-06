import { useState } from 'react'
import { AuthLayout } from '../layouts/AuthLayout'
import type { AuthCredentials } from '../types/auth'
import type { FormEvent } from 'react'

interface RegisterPageProps {
  submitting: boolean
  errorMessage: string | null
  successMessage: string | null
  onSubmit: (credentials: AuthCredentials) => Promise<void>
  onSwitchToLogin: () => void
}

export function RegisterPage({
  submitting,
  errorMessage,
  successMessage,
  onSubmit,
  onSwitchToLogin,
}: RegisterPageProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const hasMinLength = password.length >= 8
  const hasLetter = /[A-Za-z]/.test(password)
  const hasNumber = /\d/.test(password)
  const passwordMismatch = confirmPassword.length > 0 && password !== confirmPassword
  const passwordValid = hasMinLength && hasLetter && hasNumber
  const formInvalid = !passwordValid || passwordMismatch

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (submitting || formInvalid) {
      return
    }

    await onSubmit({ email, password })
  }

  return (
    <AuthLayout title="Register Page" subtitle="Tạo tài khoản để bắt đầu xây dựng kho từ vựng cá nhân.">
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

        <div className="password-checklist" aria-label="Yêu cầu mật khẩu">
          <span className={hasMinLength ? 'is-valid' : ''}>Mật khẩu có ít nhất 8 kí tự</span>
          <span className={hasLetter ? 'is-valid' : ''}>Mật khẩu có kí tự chữ cái</span>
          <span className={hasNumber ? 'is-valid' : ''}>Mật khẩu có kí tự số</span>
        </div>

        <label>
          <span className="field-label">
            Nhập lại mật khẩu <span className="required-mark">(*)</span>
          </span>
          <input
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            type="password"
            placeholder="Nhập lại mật khẩu của bạn"
            required
            disabled={submitting}
          />
        </label>

        {passwordMismatch ? <p className="form-warning">Phải khớp với mật khẩu</p> : null}
        {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
        {successMessage ? (
          <p className="form-success" role="status">
            {successMessage}
          </p>
        ) : null}

        <button type="submit" disabled={submitting || formInvalid}>
          {submitting ? 'Đang đăng ký' : 'Đăng ký'}
        </button>

        <p className="auth-switch">
          <span>Đã có tài khoản?</span>
          <button type="button" onClick={onSwitchToLogin} disabled={submitting}>
            Đăng nhập
          </button>
        </p>
      </form>
    </AuthLayout>
  )
}
