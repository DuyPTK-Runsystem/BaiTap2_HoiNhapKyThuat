import { useState } from 'react'
import type { FormEvent } from 'react'

interface CreateFolderModalProps {
  parentName: string
  submitting: boolean
  errorMessage: string | null
  onSubmit: (folderName: string) => Promise<void>
  onClose: () => void
}

export function CreateFolderModal({ parentName, submitting, errorMessage, onSubmit, onClose }: CreateFolderModalProps) {
  const [folderName, setFolderName] = useState('')
  const trimmedName = folderName.trim()

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!trimmedName || submitting) {
      return
    }

    await onSubmit(trimmedName)
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="library-modal" role="dialog" aria-modal="true" aria-labelledby="create-folder-title">
        <header>
          <h2 id="create-folder-title">Tạo Folder</h2>
          <p>Parent: {parentName}</p>
        </header>
        <form className="library-form" onSubmit={handleSubmit}>
          <label>
            <span className="field-label">
              Tên Folder <span className="required-mark">(*)</span>
            </span>
            <input
              value={folderName}
              onChange={(event) => setFolderName(event.target.value)}
              placeholder="Nhập tên folder"
              disabled={submitting}
              required
            />
          </label>
          {errorMessage ? <p className="form-error">{errorMessage}</p> : null}
          <div className="modal-actions">
            <button type="button" className="button-secondary" onClick={onClose} disabled={submitting}>
              Hủy
            </button>
            <button type="submit" disabled={!trimmedName || submitting}>
              {submitting ? 'Đang tạo' : 'Tạo Folder'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
