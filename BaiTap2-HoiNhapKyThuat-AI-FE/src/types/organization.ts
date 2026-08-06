export const itemTypes = ['FOLDER', 'VOCAB_SET'] as const

export type ItemType = (typeof itemTypes)[number]

export interface Item {
  id: number
  type: ItemType
  name: string
  description: string | null
  parentId: number | null
  vocabCount: number | null
  itemPath: string
}

export interface CreateFolderRequest {
  folderName: string
  parentId: number | null
}

export interface CreateVocabSetRequest {
  vocabSetName: string
  vocabSetDescription: string | null
  parentId: number | null
}

export interface VocabSetSummary {
  id: number
  name: string
  description: string | null
  parentId: number | null
  vocabCount: number
}

export interface BulkAddVocabsRequest {
  vocabIds: number[]
}
