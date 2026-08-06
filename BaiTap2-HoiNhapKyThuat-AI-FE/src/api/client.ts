import { ApiError } from './errors'
import { buildApiUrl } from './config'
import type { ApiErrorPayload, ApiResponse, RequestQuery } from '../types/api'

interface ApiRequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'
  body?: unknown
  query?: RequestQuery
  token?: string | null
}

function createQuery(query?: RequestQuery): Record<string, string> | undefined {
  if (!query) {
    return undefined
  }

  return Object.entries(query).reduce<Record<string, string>>((params, [key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      params[key] = String(value)
    }

    return params
  }, {})
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isApiErrorPayload(value: unknown): value is ApiErrorPayload {
  return (
    isRecord(value) &&
    typeof value.statusCode === 'number' &&
    typeof value.message === 'string' &&
    Object.prototype.hasOwnProperty.call(value, 'data')
  )
}

async function parseJsonSafely(response: Response): Promise<unknown> {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text) as unknown
  } catch {
    return null
  }
}

export async function apiRequest<TData>(path: string, options: ApiRequestOptions = {}): Promise<TData> {
  const { method = 'GET', body, query, token } = options
  const headers = new Headers()
  const isFormData = body instanceof FormData

  if (!isFormData && body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(buildApiUrl(path, createQuery(query)), {
    method,
    headers,
    body: isFormData ? body : body === undefined ? undefined : JSON.stringify(body),
    credentials: 'include',
  })

  const payload = await parseJsonSafely(response)

  if (!response.ok) {
    if (isApiErrorPayload(payload)) {
      throw new ApiError(payload.message, payload.statusCode, payload.error, payload)
    }

    throw new ApiError(response.statusText || 'Không thể gọi API', response.status, null, null)
  }

  if (isRecord(payload) && Object.prototype.hasOwnProperty.call(payload, 'data')) {
    return (payload as unknown as ApiResponse<TData>).data
  }

  return payload as TData
}
