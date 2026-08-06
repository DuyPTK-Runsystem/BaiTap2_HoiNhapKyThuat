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

export interface BulkImportFailure {
  rowNumber: number
  word?: string | null
  error: string
}

export interface BulkImportVocabResponse {
  total: number
  success: number
  failed: number
  items?: unknown[]
  failures?: BulkImportFailure[]
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
