import { useState } from 'react'
import type { FormEvent } from 'react'

interface CreateVocabSetModalProps {
  parentName: string
  submitting: boolean
  errorMessage: string | null
  onSubmit: (name: string, description: string | null) => Promise<void>
  onClose: () => void
}

export function CreateVocabSetModal({
  parentName,
  submitting,
  errorMessage,
  onSubmit,
  onClose,
}: CreateVocabSetModalProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const trimmedName = name.trim()
  const trimmedDescription = description.trim()

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!trimmedName || submitting) {
      return
    }

    await onSubmit(trimmedName, trimmedDescription || null)
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="library-modal" role="dialog" aria-modal="true" aria-labelledby="create-set-title">
        <header>
          <h2 id="create-set-title">Tạo VocabSet</h2>
          <p>Parent: {parentName}</p>
        </header>
        <form className="library-form" onSubmit={handleSubmit}>
          <label>
            <span className="field-label">
              Tên VocabSet <span className="required-mark">(*)</span>
            </span>
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Nhập tên vocab set"
              disabled={submitting}
              required
            />
          </label>
          <label>
            Mô tả
            <textarea
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Nhập mô tả ngắn"
              disabled={submitting}
              rows={3}
            />
          </label>
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          <div className="modal-actions">
            <button type="button" className="button-secondary" onClick={onClose} disabled={submitting}>
              Hủy
            </button>
            <button type="submit" disabled={!trimmedName || submitting}>
              {submitting ? 'Đang tạo' : 'Tạo VocabSet'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
