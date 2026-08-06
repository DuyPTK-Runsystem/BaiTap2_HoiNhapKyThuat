export const flashcardFrontTypes = ['MEANING', 'AUDIO'] as const

export type FlashcardFrontType = (typeof flashcardFrontTypes)[number]

export interface Flashcard {
  vocabId: number
  frontType: FlashcardFrontType
  frontText: string | null
  frontAudioUrl: string | null
  backWord: string
  backMeaning: string | null
  backAudioUrl: string | null
}

export interface CreateFlashcardsRequest {
  sourceItemIds: number[] | null
  numberOfFlashcards: number
}

export interface CreateFlashcardsResponse {
  sourceItemIds: number[] | null
  numberOfFlashcards: number
  flashcards: Flashcard[]
}
