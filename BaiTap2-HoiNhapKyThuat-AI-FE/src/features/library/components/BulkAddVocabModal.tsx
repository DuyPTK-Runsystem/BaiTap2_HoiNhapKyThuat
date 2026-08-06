import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import type { BulkAddVocabsToSetResponse } from '../../../types/vocabulary'

interface BulkAddVocabModalProps {
  vocabSetName: string
  submitting: boolean
  errorMessage: string | null
  result: BulkAddVocabsToSetResponse | null
  onSubmit: (vocabIds: number[]) => Promise<void>
  onClose: () => void
}

function parseIds(value: string): number[] {
  return value
    .split(/[,\s]+/)
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isInteger(item) && item > 0)
}

export function BulkAddVocabModal({
  vocabSetName,
  submitting,
  errorMessage,
  result,
  onSubmit,
  onClose,
}: BulkAddVocabModalProps) {
  const [value, setValue] = useState('')
  const vocabIds = useMemo(() => parseIds(value), [value])
  const hasRawValue = value.trim().length > 0

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (vocabIds.length === 0 || submitting) {
      return
    }

    await onSubmit(vocabIds)
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="library-modal" role="dialog" aria-modal="true" aria-labelledby="bulk-add-title">
        <header>
          <h2 id="bulk-add-title">Bulk Add Vocab</h2>
          <p>VocabSet: {vocabSetName}</p>
        </header>
        <form className="library-form" onSubmit={handleSubmit}>
          <label>
            <span className="field-label">
              Danh sách Vocab ID <span className="required-mark">(*)</span>
            </span>
            <textarea
              value={value}
              onChange={(event) => setValue(event.target.value)}
              placeholder="Ví dụ: 1, 2, 3"
              disabled={submitting}
              rows={4}
              required
            />
          </label>
          {hasRawValue && vocabIds.length === 0 ? <p className="form-warning">Cần ít nhất một ID là số nguyên dương.</p> : null}
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          {result ? (
            <div className="bulk-result">
              <strong>
                Success {result.success}/{result.total}, Failed {result.failed}
              </strong>
              <ul>
                {result.items.map((item) => (
                  <li key={item.vocabId}>
                    #{item.vocabId}: {item.success ? (item.added ? 'Đã thêm' : 'Đã tồn tại') : item.error ?? 'Thất bại'}
                  </li>
                ))}
              </ul>
            </div>
          ) : null}
          <div className="modal-actions">
            <button type="button" className="button-secondary" onClick={onClose} disabled={submitting}>
              Đóng
            </button>
            <button type="submit" disabled={vocabIds.length === 0 || submitting}>
              {submitting ? 'Đang thêm' : 'Bulk Add'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
