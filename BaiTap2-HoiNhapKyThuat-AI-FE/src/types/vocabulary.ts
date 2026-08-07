import type { VocabSetSummary } from './organization'

export interface Vocab {
  id: number
  word: string
  meaning: string | null
  ipa: string | null
  audioUrl?: string | null
  audio_url?: string | null
  mastered: boolean
}

export interface VocabPaginationMeta {
  page: number
  pageSize: number
  totalPages: number
  totalItems: number
}

export interface PaginatedVocabs {
  meta: VocabPaginationMeta
  result: Vocab[]
}

export interface CreateVocabRequest {
  word: string
  meaning: string
  ipa?: string | null
}

export interface UpdateVocabMeaningRequest {
  meaning: string
}

export interface CreateVocabInSetResponse {
  vocabSet: VocabSetSummary
  vocab: Vocab
  added: boolean
}

export interface BulkImportItem {
  rowNumber: number
  word: string | null
  success: boolean
  vocab?: Vocab
  error?: string
}

export interface BulkImportVocabResponse {
  total_rows: number
  success_count: number
  failure_count: number
  items: BulkImportItem[]
}

export interface AddVocabToSetResponse {
  vocabSet: VocabSetSummary
  vocab: Vocab
  added: boolean
}

export interface BulkAddVocabItemResult {
  vocabId: number
  success: boolean
  added: boolean
  vocab?: Vocab
  error?: string
}

export interface BulkAddVocabsToSetResponse {
  vocabSet: VocabSetSummary
  total: number
  success: number
  failed: number
  items: BulkAddVocabItemResult[]
}
