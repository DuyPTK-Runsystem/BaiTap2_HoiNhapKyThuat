import type { ApiErrorPayload } from '../types/api'

export class ApiError extends Error {
  readonly statusCode: number
  readonly error: string | null
  readonly payload: ApiErrorPayload | null

  constructor(message: string, statusCode: number, error: string | null, payload: ApiErrorPayload | null) {
    super(message)
    this.name = 'ApiError'
    this.statusCode = statusCode
    this.error = error
    this.payload = payload
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}
