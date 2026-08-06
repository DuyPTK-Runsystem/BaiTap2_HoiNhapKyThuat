export interface User {
  id: number
  email: string
}

export type AuthStatus = 'checking' | 'authenticated' | 'guest'

export type AuthView = 'login' | 'register'

export interface AuthCredentials {
  email: string
  password: string
}

export type RegisterRequest = AuthCredentials

export type LoginRequest = AuthCredentials

export interface RegisterResponse {
  id: number
  email: string
}

export interface AuthSessionResponse {
  access_token: string
  user: User
}
