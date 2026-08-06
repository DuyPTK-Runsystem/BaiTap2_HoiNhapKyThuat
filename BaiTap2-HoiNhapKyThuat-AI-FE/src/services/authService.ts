import { apiRequest } from '../api/client'
import type { AuthSessionResponse, LoginRequest, RegisterRequest, RegisterResponse, User } from '../types/auth'

export function register(request: RegisterRequest): Promise<RegisterResponse> {
  return apiRequest<RegisterResponse>('/api/v1/auth/register', {
    method: 'POST',
    body: request,
  })
}

export function login(request: LoginRequest): Promise<AuthSessionResponse> {
  return apiRequest<AuthSessionResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: request,
  })
}

export function getAccount(token: string): Promise<User> {
  return apiRequest<User>('/api/v1/auth/account', {
    token,
  })
}

export function refreshSession(): Promise<AuthSessionResponse> {
  return apiRequest<AuthSessionResponse>('/api/v1/auth/refresh')
}

export function logout(token: string | null): Promise<null> {
  return apiRequest<null>('/api/v1/auth/logout', {
    method: 'POST',
    token,
  })
}
