import { buildAssetUrl } from '../api/config'
import { apiRequest } from '../api/client'
import type {
  BulkImportVocabResponse,
  CreateVocabInSetResponse,
  CreateVocabRequest,
  PaginatedVocabs,
  UpdateVocabMeaningRequest,
  Vocab,
} from '../types/vocabulary'

export function getVocabs(vocabSetId: number | undefined, page: number, size: number, token: string): Promise<PaginatedVocabs> {
  return apiRequest<PaginatedVocabs>('/api/v1/vocabs', {
    query: { vocabSetId, page, size },
    token,
  })
}

export function createVocab(
  request: CreateVocabRequest,
  token: string,
  vocabSetId?: number,
): Promise<Vocab | CreateVocabInSetResponse> {
  return apiRequest<Vocab | CreateVocabInSetResponse>('/api/v1/vocabs', {
    method: 'POST',
    body: request,
    query: { vocabSetId },
    token,
  })
}

export function bulkImportVocabs(
  file: File,
  token: string,
  vocabSetId?: number,
): Promise<BulkImportVocabResponse> {
  const formData = new FormData()
  formData.append('file', file)

  return apiRequest<BulkImportVocabResponse>('/api/v1/vocabs/bulk', {
    method: 'POST',
    body: formData,
    query: { vocabSetId },
    token,
  })
}

export function getVocabById(vocabId: number, token: string): Promise<Vocab> {
  return apiRequest<Vocab>('/api/v1/vocabs/lookup', {
    query: { id: vocabId },
    token,
  })
}

export function getVocabByWord(word: string, token: string): Promise<Vocab> {
  return apiRequest<Vocab>('/api/v1/vocabs/lookup', {
    query: { word },
    token,
  })
}

export function updateVocabMeaningById(
  vocabId: number,
  request: UpdateVocabMeaningRequest,
  token: string,
): Promise<Vocab> {
  return apiRequest<Vocab>('/api/v1/vocabs/lookup', {
    method: 'PATCH',
    body: request,
    query: { id: vocabId },
    token,
  })
}

export function updateVocabMeaningByWord(
  word: string,
  request: UpdateVocabMeaningRequest,
  token: string,
): Promise<Vocab> {
  return apiRequest<Vocab>('/api/v1/vocabs/lookup', {
    method: 'PATCH',
    body: request,
    query: { word },
    token,
  })
}

export function getAudioUrl(audioPath: string | null | undefined): string | null {
  return buildAssetUrl(audioPath)
}

export async function fetchAudioBlob(
  audioPath: string,
  token: string,
  signal?: AbortSignal,
): Promise<Blob> {
  const audioUrl = buildAssetUrl(audioPath)

  if (!audioUrl) {
    throw new Error('Audio URL không hợp lệ.')
  }

  const response = await fetch(audioUrl, {
    headers: { Authorization: `Bearer ${token}` },
    credentials: 'include',
    signal,
  })

  if (!response.ok) {
    throw new Error('Không thể tải audio.')
  }

  return response.blob()
}
