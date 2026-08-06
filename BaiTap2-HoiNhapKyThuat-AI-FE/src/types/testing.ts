export interface TestOption {
  id: number
  optionOrder: number
  optionContent: string
  correct: boolean
  audioUrl: string | null
}

export interface TestAnswer {
  id: number
  questionId: number
  selectedOptionId: number | null
  selectedOptionContent: string | null
  correct: boolean
}

export interface TestQuestion {
  id: number
  vocabId: number
  questionContent: string
  correctAnswer: string
  audioUrl: string | null
  options: TestOption[]
  answer: TestAnswer | null
}

export interface TestSession {
  id: number
  numberOfQuestion: number
  timeInMinute: number | null
  correctAnswerCount: number
  incorrectAnswerCount: number
  remainingTimeInSeconds: number | null
  finished: boolean
  questions: TestQuestion[]
}

export interface CreateTestRequest {
  sourceItemIds: number[] | null
  numberOfQuestion: number
  timeInMinute: number | null
}

export interface FinishTestAnswerRequest {
  questionId: number
  optionId: number
}

export interface FinishTestRequest {
  answers: FinishTestAnswerRequest[]
}
