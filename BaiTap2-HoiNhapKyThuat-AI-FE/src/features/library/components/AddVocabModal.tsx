import { useState } from 'react'
import type { FormEvent } from 'react'
import type { CreateVocabRequest } from '../../../types/vocabulary'

interface AddVocabModalProps {
  vocabSetName: string
  submitting: boolean
  errorMessage: string | null
  onSubmit: (request: CreateVocabRequest) => Promise<void>
  onClose: () => void
}

export function AddVocabModal({ vocabSetName, submitting, errorMessage, onSubmit, onClose }: AddVocabModalProps) {
  const [word, setWord] = useState('')
  const [meaning, setMeaning] = useState('')
  const [ipa, setIpa] = useState('')
  const trimmedWord = word.trim()
  const trimmedMeaning = meaning.trim()
  const trimmedIpa = ipa.trim()
  const valid = trimmedWord.length > 0 && trimmedMeaning.length > 0

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!valid || submitting) {
      return
    }

    await onSubmit({
      word: trimmedWord,
      meaning: trimmedMeaning,
      ipa: trimmedIpa || null,
    })
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="library-modal" role="dialog" aria-modal="true" aria-labelledby="add-vocab-title">
        <header>
          <h2 id="add-vocab-title">Thêm Vocab</h2>
          <p>VocabSet: {vocabSetName}</p>
        </header>
        <form className="library-form" onSubmit={handleSubmit}>
          <label>
            <span className="field-label">
              Từ vựng <span className="required-mark">(*)</span>
            </span>
            <input
              value={word}
              onChange={(event) => setWord(event.target.value)}
              placeholder="Nhập từ vựng, ví dụ: agenda"
              disabled={submitting}
              required
            />
          </label>
          <label>
            <span className="field-label">
              Nghĩa <span className="required-mark">(*)</span>
            </span>
            <textarea
              value={meaning}
              onChange={(event) => setMeaning(event.target.value)}
              placeholder="Nhập nghĩa của từ"
              disabled={submitting}
              rows={3}
              required
            />
          </label>
          <label>
            <span className="field-label">IPA</span>
            <input
              value={ipa}
              onChange={(event) => setIpa(event.target.value)}
              placeholder="/əˈdʒendə/"
              disabled={submitting}
            />
          </label>
          {(word || meaning) && !valid ? <p className="form-warning">Cần nhập từ vựng và nghĩa.</p> : null}
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          <div className="modal-actions">
            <button type="button" className="button-secondary" onClick={onClose} disabled={submitting}>
              Hủy
            </button>
            <button type="submit" disabled={!valid || submitting}>
              {submitting ? 'Đang tạo' : 'Tạo Vocab'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
