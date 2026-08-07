import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import type { BulkImportVocabResponse } from '../../../types/vocabulary'

interface BulkAddVocabModalProps {
  vocabSetName: string
  submitting: boolean
  errorMessage: string | null
  result: BulkImportVocabResponse | null
  onSubmit: (file: File) => Promise<void>
  onClose: () => void
}

function isXlsxFile(file: File): boolean {
  return file.name.toLowerCase().endsWith('.xlsx')
}

function formatFileSize(size: number): string {
  if (size < 1024) {
    return `${size} B`
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }

  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

export function BulkAddVocabModal({
  vocabSetName,
  submitting,
  errorMessage,
  result,
  onSubmit,
  onClose,
}: BulkAddVocabModalProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [fileError, setFileError] = useState<string | null>(null)
  const valid = selectedFile !== null && isXlsxFile(selectedFile)

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.currentTarget.files?.[0] ?? null

    setSelectedFile(file)
    setFileError(file && !isXlsxFile(file) ? 'Chỉ hỗ trợ file .xlsx.' : null)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!selectedFile) {
      setFileError('Cần chọn file .xlsx để import.')
      return
    }

    if (!isXlsxFile(selectedFile)) {
      setFileError('Chỉ hỗ trợ file .xlsx.')
      return
    }

    if (submitting) {
      return
    }

    await onSubmit(selectedFile)
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
              File import <span className="required-mark">(*)</span>
            </span>
            <input
              className="library-file-input"
              type="file"
              accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              onChange={handleFileChange}
              disabled={submitting}
              required
            />
          </label>
          <p className="library-muted">Định dạng hỗ trợ: .xlsx. Cột bắt buộc: word, meaning. Cột tùy chọn: ipa.</p>
          {selectedFile ? (
            <div className="library-file-summary" aria-live="polite">
              <strong>{selectedFile.name}</strong>
              <span>{formatFileSize(selectedFile.size)}</span>
            </div>
          ) : null}
          {fileError ? <p className="form-warning">{fileError}</p> : null}
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          {result ? (
            <div className="bulk-result">
              <strong>Kết quả import</strong>
              <div className="bulk-result-counts" aria-live="polite">
                <span>Tổng số dòng: {result.total_rows}</span>
                <span>Thành công: {result.success_count}</span>
                <span>Thất bại: {result.failure_count}</span>
              </div>
              {result.items.filter((item) => !item.success).length > 0 ? (
                <ul>
                  {result.items
                    .filter((item) => !item.success)
                    .map((failure) => (
                    <li key={`${failure.rowNumber}-${failure.word ?? 'empty'}-${failure.error ?? 'unknown'}`}>
                      Dòng {failure.rowNumber}
                      {failure.word ? ` (${failure.word})` : ''}: {failure.error ?? 'Không rõ lỗi'}
                    </li>
                    ))}
                </ul>
              ) : null}
            </div>
          ) : null}
          <div className="modal-actions">
            <button type="button" className="button-secondary" onClick={onClose} disabled={submitting}>
              Đóng
            </button>
            <button type="submit" disabled={!valid || submitting}>
              {submitting ? 'Đang import' : 'Bulk Add'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
