import { useCallback, useEffect, useState } from 'react'
import { isApiError } from '../api/errors'
import { getAccount, login, logout, refreshSession, register } from '../services/authService'
import { readStorageValue, removeStorageValue, writeStorageValue } from '../utils/storage'
import type { AuthCredentials, AuthStatus, User } from '../types/auth'

const ACCESS_TOKEN_STORAGE_KEY = 'vocab_library_access_token'

interface AuthSessionState {
  user: User | null
  accessToken: string | null
  status: AuthStatus
  submitting: boolean
  errorMessage: string | null
  successMessage: string | null
}

export interface AuthSession extends AuthSessionState {
  isAuthenticated: boolean
  isChecking: boolean
  signIn: (credentials: AuthCredentials) => Promise<void>
  signUp: (credentials: AuthCredentials) => Promise<void>
  signOut: () => Promise<void>
  refresh: () => Promise<void>
  clearError: () => void
  clearSuccess: () => void
}

export function useAuthSession(): AuthSession {
  const [state, setState] = useState<AuthSessionState>(() => {
    const accessToken = readStorageValue(ACCESS_TOKEN_STORAGE_KEY)

    return {
      user: null,
      accessToken,
      status: accessToken ? 'checking' : 'guest',
      submitting: false,
      errorMessage: null,
      successMessage: null,
    }
  })

  const setSession = useCallback((accessToken: string, user: User, successMessage: string | null = null) => {
    writeStorageValue(ACCESS_TOKEN_STORAGE_KEY, accessToken)
    setState({
      user,
      accessToken,
      status: 'authenticated',
      submitting: false,
      errorMessage: null,
      successMessage,
    })
  }, [])

  const clearSession = useCallback((errorMessage: string | null = null) => {
    removeStorageValue(ACCESS_TOKEN_STORAGE_KEY)
    setState({
      user: null,
      accessToken: null,
      status: 'guest',
      submitting: false,
      errorMessage,
      successMessage: null,
    })
  }, [])

  useEffect(() => {
    const token = readStorageValue(ACCESS_TOKEN_STORAGE_KEY)

    if (!token) {
      return
    }

    const savedAccessToken = token
    let active = true

    async function restoreSession() {
      try {
        const user = await getAccount(savedAccessToken)

        if (active) {
          setSession(savedAccessToken, user)
        }
      } catch (error) {
        if (isApiError(error) && error.statusCode === 401) {
          try {
            const session = await refreshSession()

            if (active) {
              setSession(session.access_token, session.user)
            }

            return
          } catch {
            if (active) {
              clearSession()
            }

            return
          }
        }

        if (active) {
          clearSession()
        }
      }
    }

    void restoreSession()

    return () => {
      active = false
    }
  }, [clearSession, setSession])

  const signIn = useCallback(
    async (credentials: AuthCredentials) => {
      if (state.submitting) {
        return
      }

      setState((current) => ({ ...current, submitting: true, errorMessage: null, successMessage: null }))

      try {
        const session = await login(credentials)
        setSession(session.access_token, session.user, 'Đăng nhập thành công')
      } catch (error) {
        setState((current) => ({
          ...current,
          status: 'guest',
          submitting: false,
          errorMessage: error instanceof Error ? error.message : 'Đăng nhập thất bại',
          successMessage: null,
        }))
      }
    },
    [setSession, state.submitting],
  )

  const signUp = useCallback(
    async (credentials: AuthCredentials) => {
      if (state.submitting) {
        return
      }

      setState((current) => ({ ...current, submitting: true, errorMessage: null, successMessage: null }))

      try {
        await register(credentials)
        removeStorageValue(ACCESS_TOKEN_STORAGE_KEY)
        setState({
          user: null,
          accessToken: null,
          status: 'guest',
          submitting: false,
          errorMessage: null,
          successMessage: 'Đăng ký thành công. Đang chuyển về trang đăng nhập.',
        })
      } catch (error) {
        setState((current) => ({
          ...current,
          status: 'guest',
          submitting: false,
          errorMessage: error instanceof Error ? error.message : 'Đăng ký thất bại',
          successMessage: null,
        }))
      }
    },
    [state.submitting],
  )

  const signOut = useCallback(async () => {
    if (state.submitting) {
      return
    }

    setState((current) => ({ ...current, submitting: true, errorMessage: null, successMessage: null }))

    try {
      await logout(state.accessToken)
    } finally {
      clearSession()
    }
  }, [clearSession, state.accessToken, state.submitting])

  const refresh = useCallback(async () => {
    setState((current) => ({ ...current, status: 'checking', errorMessage: null, successMessage: null }))

    try {
      const session = await refreshSession()
      setSession(session.access_token, session.user)
    } catch (error) {
      clearSession(error instanceof Error ? error.message : 'Không thể làm mới phiên đăng nhập')
    }
  }, [clearSession, setSession])

  const clearError = useCallback(() => {
    setState((current) => ({ ...current, errorMessage: null }))
  }, [])

  const clearSuccess = useCallback(() => {
    setState((current) => ({ ...current, successMessage: null }))
  }, [])

  return {
    ...state,
    isAuthenticated: state.status === 'authenticated' && Boolean(state.accessToken && state.user),
    isChecking: state.status === 'checking',
    signIn,
    signUp,
    signOut,
    refresh,
    clearError,
    clearSuccess,
  }
}
