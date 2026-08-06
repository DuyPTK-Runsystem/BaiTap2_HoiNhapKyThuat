import { apiRequest } from '../api/client'
import type { CreateFlashcardsRequest, CreateFlashcardsResponse } from '../types/flashcard'

export function createFlashcards(
  request: CreateFlashcardsRequest,
  token: string,
): Promise<CreateFlashcardsResponse> {
  return apiRequest<CreateFlashcardsResponse>('/api/v1/flashcards', {
    method: 'POST',
    body: request,
    token,
  })
}
