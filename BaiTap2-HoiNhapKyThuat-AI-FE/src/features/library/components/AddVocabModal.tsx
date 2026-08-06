import { useState } from 'react'
import type { FormEvent } from 'react'

interface AddVocabModalProps {
  vocabSetName: string
  submitting: boolean
  errorMessage: string | null
  onSubmit: (vocabId: number) => Promise<void>
  onClose: () => void
}

export function AddVocabModal({ vocabSetName, submitting, errorMessage, onSubmit, onClose }: AddVocabModalProps) {
  const [value, setValue] = useState('')
  const vocabId = Number(value)
  const valid = Number.isInteger(vocabId) && vocabId > 0

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!valid || submitting) {
      return
    }

    await onSubmit(vocabId)
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
              Vocab ID <span className="required-mark">(*)</span>
            </span>
            <input
              value={value}
              onChange={(event) => setValue(event.target.value)}
              inputMode="numeric"
              placeholder="Nhập ID vocab đã tồn tại"
              disabled={submitting}
              required
            />
          </label>
          {value && !valid ? <p className="form-warning">Vocab ID phải là số nguyên dương.</p> : null}
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          <div className="modal-actions">
            <button type="button" className="button-secondary" onClick={onClose} disabled={submitting}>
              Hủy
            </button>
            <button type="submit" disabled={!valid || submitting}>
              {submitting ? 'Đang thêm' : 'Thêm Vocab'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
