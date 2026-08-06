export interface ApiResponse<TData> {
  statusCode: number
  error: string | null
  message: string
  data: TData
}

export interface ApiErrorPayload {
  statusCode: number
  error: string | null
  message: string
  data: null
}

export type RequestQueryValue = string | number | boolean | null | undefined

export type RequestQuery = Record<string, RequestQueryValue>
