import { buildAssetUrl } from '../api/config'
import { apiRequest } from '../api/client'
import type {
  BulkImportVocabResponse,
  CreateVocabInSetResponse,
  CreateVocabRequest,
  UpdateVocabMeaningRequest,
  Vocab,
} from '../types/vocabulary'

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
