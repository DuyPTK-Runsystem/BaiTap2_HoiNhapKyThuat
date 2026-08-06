import { apiRequest } from '../api/client'
import type {
  BulkAddVocabsRequest,
  CreateFolderRequest,
  CreateVocabSetRequest,
  Item,
} from '../types/organization'
import type { AddVocabToSetResponse, BulkAddVocabsToSetResponse } from '../types/vocabulary'

export function createFolder(request: CreateFolderRequest, token: string): Promise<Item> {
  return apiRequest<Item>('/api/v1/folders', {
    method: 'POST',
    body: request,
    token,
  })
}

export function createVocabSet(request: CreateVocabSetRequest, token: string): Promise<Item> {
  return apiRequest<Item>('/api/v1/vocab-sets', {
    method: 'POST',
    body: request,
    token,
  })
}

export function getChildren(parentId: number | null, token: string): Promise<Item[]> {
  return apiRequest<Item[]>('/api/v1/items/children', {
    query: { parentId },
    token,
  })
}

export function searchItems(name: string, token: string): Promise<Item[]> {
  return apiRequest<Item[]>('/api/v1/items/search', {
    query: { name },
    token,
  })
}

export function getItemByPath(path: string, token: string): Promise<Item> {
  return apiRequest<Item>('/api/v1/items/by-path', {
    query: { path },
    token,
  })
}

export function addVocabToVocabSet(
  vocabSetId: number,
  vocabId: number,
  token: string,
): Promise<AddVocabToSetResponse> {
  return apiRequest<AddVocabToSetResponse>(`/api/v1/vocab-sets/${vocabSetId}/vocabs/${vocabId}`, {
    method: 'POST',
    token,
  })
}

export function bulkAddVocabsToVocabSet(
  vocabSetId: number,
  request: BulkAddVocabsRequest,
  token: string,
): Promise<BulkAddVocabsToSetResponse> {
  return apiRequest<BulkAddVocabsToSetResponse>(`/api/v1/vocab-sets/${vocabSetId}/vocabs/bulk`, {
    method: 'POST',
    body: request,
    token,
  })
}
