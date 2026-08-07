export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

export function buildApiUrl(path: string, query?: Record<string, string>): string {
  const url = new URL(path, API_BASE_URL)

  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      url.searchParams.set(key, value)
    })
  }

  return url.toString()
}

export function buildAssetUrl(path: string | null | undefined): string | null {
  if (!path) {
    return null
  }

  return new URL(path, API_BASE_URL).toString()
}
