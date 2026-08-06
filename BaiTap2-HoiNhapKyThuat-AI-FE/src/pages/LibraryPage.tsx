import { useCallback, useEffect, useState } from 'react'
import type { MouseEvent } from 'react'
import { useLocation, useOutletContext } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'
import { AddVocabModal } from '../features/library/components/AddVocabModal'
import { BulkAddVocabModal } from '../features/library/components/BulkAddVocabModal'
import { CreateFolderModal } from '../features/library/components/CreateFolderModal'
import { CreateVocabSetModal } from '../features/library/components/CreateVocabSetModal'
import { LibraryTree } from '../features/library/components/LibraryTree'
import { VocabSetDetail } from '../features/library/components/VocabSetDetail'
import {
  createFolder,
  createVocabSet,
  getChildren,
} from '../services/organizationService'
import { bulkImportVocabs, createVocab } from '../services/vocabularyService'
import type { Item } from '../types/organization'
import type {
  BulkImportVocabResponse,
  CreateVocabInSetResponse,
  CreateVocabRequest,
  Vocab,
} from '../types/vocabulary'

interface ProtectedOutletContext {
  accessToken: string | null
}

type LibraryModal = 'folder' | 'vocabSet' | 'addVocab' | 'bulkAdd' | null

interface LibraryLocationState {
  selectedItem?: Item
}

function errorMessageOf(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback
}

const selectionBoundarySelector = '.library-toolbar, .library-tree-panel, .library-detail-panel, .modal-backdrop'

function isCreateVocabInSetResponse(value: Vocab | CreateVocabInSetResponse): value is CreateVocabInSetResponse {
  return 'vocab' in value && 'vocabSet' in value && 'added' in value
}

function normalizeCreateVocabResult(
  result: Vocab | CreateVocabInSetResponse,
  vocabSet: Item,
): CreateVocabInSetResponse {
  if (isCreateVocabInSetResponse(result)) {
    return result
  }

  return {
    vocabSet: {
      id: vocabSet.id,
      name: vocabSet.name,
      description: vocabSet.description,
      parentId: vocabSet.parentId,
      vocabCount: vocabSet.vocabCount ?? 0,
    },
    vocab: result,
    added: true,
  }
}

export function LibraryPage() {
  const { accessToken } = useOutletContext<ProtectedOutletContext>()
  const location = useLocation()
  const locationState = location.state as LibraryLocationState | null
  const [rootItems, setRootItems] = useState<Item[]>([])
  const [childrenByParentId, setChildrenByParentId] = useState<Record<number, Item[]>>({})
  const [expandedFolderIds, setExpandedFolderIds] = useState<Set<number>>(new Set())
  const [loadingFolderIds, setLoadingFolderIds] = useState<Set<number>>(new Set())
  const [selectedItem, setSelectedItem] = useState<Item | null>(locationState?.selectedItem ?? null)
  const [loadingRoot, setLoadingRoot] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [modal, setModal] = useState<LibraryModal>(null)
  const [modalError, setModalError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [lastAddResult, setLastAddResult] = useState<CreateVocabInSetResponse | null>(null)
  const [bulkResult, setBulkResult] = useState<BulkImportVocabResponse | null>(null)

  const selectedFolder = selectedItem?.type === 'FOLDER' ? selectedItem : null
  const selectedVocabSet = selectedItem?.type === 'VOCAB_SET' ? selectedItem : null
  const createParentId = selectedFolder?.id ?? null
  const createParentName = selectedFolder?.name ?? 'Root'

  const loadRoot = useCallback(async () => {
    if (!accessToken) {
      return
    }

    try {
      const items = await getChildren(null, accessToken)
      setRootItems(items)
    } catch (error) {
      setErrorMessage(errorMessageOf(error, 'Không thể tải thư viện.'))
    } finally {
      setLoadingRoot(false)
    }
  }, [accessToken])

  const loadFolderChildren = useCallback(
    async (folderId: number) => {
      if (!accessToken) {
        return
      }

      setLoadingFolderIds((current) => new Set(current).add(folderId))
      setErrorMessage(null)

      try {
        const items = await getChildren(folderId, accessToken)
        setChildrenByParentId((current) => ({ ...current, [folderId]: items }))
      } catch (error) {
        setErrorMessage(errorMessageOf(error, 'Không thể tải nội dung folder.'))
      } finally {
        setLoadingFolderIds((current) => {
          const next = new Set(current)
          next.delete(folderId)
          return next
        })
      }
    },
    [accessToken],
  )

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void loadRoot()
    }, 0)

    return () => window.clearTimeout(timeoutId)
  }, [loadRoot])

  useEffect(() => {
    if (locationState?.selectedItem) {
      queueMicrotask(() => setSelectedItem(locationState.selectedItem ?? null))
    }
  }, [locationState?.selectedItem])

  function handleToggleFolder(folder: Item) {
    setExpandedFolderIds((current) => {
      const next = new Set(current)

      if (next.has(folder.id)) {
        next.delete(folder.id)
      } else {
        next.add(folder.id)

        if (!childrenByParentId[folder.id]) {
          void loadFolderChildren(folder.id)
        }
      }

      return next
    })
  }

  function openModal(nextModal: LibraryModal) {
    setModalError(null)
    setBulkResult(null)
    setModal(nextModal)
  }

  async function refreshAfterMutation(parentId: number | null) {
    if (parentId === null) {
      await loadRoot()
      return
    }

    await loadFolderChildren(parentId)
  }

  async function handleCreateFolder(folderName: string) {
    if (!accessToken || submitting) {
      return
    }

    setSubmitting(true)
    setModalError(null)

    try {
      const item = await createFolder({ folderName, parentId: createParentId }, accessToken)
      setSuccessMessage(`Đã tạo Folder ${item.name}.`)
      setSelectedItem(item)
      setModal(null)
      await refreshAfterMutation(createParentId)
    } catch (error) {
      setModalError(errorMessageOf(error, 'Không thể tạo Folder.'))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleCreateVocabSet(vocabSetName: string, vocabSetDescription: string | null) {
    if (!accessToken || submitting) {
      return
    }

    setSubmitting(true)
    setModalError(null)

    try {
      const item = await createVocabSet({ vocabSetName, vocabSetDescription, parentId: createParentId }, accessToken)
      setSuccessMessage(`Đã tạo VocabSet ${item.name}.`)
      setSelectedItem(item)
      setModal(null)
      await refreshAfterMutation(createParentId)
    } catch (error) {
      setModalError(errorMessageOf(error, 'Không thể tạo VocabSet.'))
    } finally {
      setSubmitting(false)
    }
  }

  function handleLibraryMouseDown(event: MouseEvent<HTMLElement>) {
    if (!(event.target instanceof Element)) {
      return
    }

    if (event.target.closest(selectionBoundarySelector)) {
      return
    }

    setSelectedItem(null)
  }

  async function handleAddVocab(request: CreateVocabRequest) {
    if (!accessToken || !selectedVocabSet || submitting) {
      return
    }

    setSubmitting(true)
    setModalError(null)

    try {
      const result = await createVocab(request, accessToken, selectedVocabSet.id)
      const normalizedResult = normalizeCreateVocabResult(result, selectedVocabSet)
      setLastAddResult(normalizedResult)
      setSuccessMessage(`Đã tạo Vocab "${normalizedResult.vocab.word}".`)
      setModal(null)
      await refreshAfterMutation(selectedVocabSet.parentId)
    } catch (error) {
      setModalError(errorMessageOf(error, 'Không thể tạo Vocab.'))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleBulkAdd(file: File) {
    if (!accessToken || !selectedVocabSet || submitting) {
      return
    }

    setSubmitting(true)
    setModalError(null)

    try {
      const result = await bulkImportVocabs(file, accessToken, selectedVocabSet.id)
      setBulkResult(result)
      setSuccessMessage(`Bulk import: ${result.success}/${result.total} thành công.`)
      await refreshAfterMutation(selectedVocabSet.parentId)
    } catch (error) {
      setModalError(errorMessageOf(error, 'Không thể bulk import Vocab.'))
    } finally {
      setSubmitting(false)
    }
  }

  let content = (
    <LibraryTree
      rootItems={rootItems}
      childrenByParentId={childrenByParentId}
      expandedFolderIds={expandedFolderIds}
      selectedItemId={selectedItem?.id ?? null}
      loadingFolderIds={loadingFolderIds}
      onToggleFolder={handleToggleFolder}
      onSelectItem={setSelectedItem}
    />
  )

  if (loadingRoot) {
    content = <StatusMessage tone="loading" title="Đang tải thư viện" message="Đang lấy danh sách item root." />
  } else if (errorMessage) {
    content = <StatusMessage tone="error" title="Không thể tải Library" message={errorMessage} />
  }

  return (
    <section className="library-page" onMouseDown={handleLibraryMouseDown}>
      <div className="library-toolbar">
        <div>
          <p className="eyebrow">Organization</p>
          <h2>Vocabulary Library</h2>
          <p>Quản lý Folder và VocabSet theo cấu trúc cây.</p>
        </div>
        <div className="library-actions">
          <button type="button" onClick={() => openModal('folder')}>
            Tạo Folder
          </button>
          <button type="button" className="button-secondary" onClick={() => openModal('vocabSet')}>
            Tạo VocabSet
          </button>
        </div>
      </div>

      {successMessage ? <p className="form-success">{successMessage}</p> : null}

      <div className="library-grid">
        <aside className="library-tree-panel">{content}</aside>
        <main className="library-detail-panel">
          {!selectedItem ? (
            <StatusMessage tone="empty" title="Chưa chọn item" message="Chọn Folder hoặc VocabSet trong cây để xem chi tiết." />
          ) : selectedItem.type === 'FOLDER' ? (
            <section className="library-detail-card">
              <p className="eyebrow">Folder</p>
              <h2>{selectedItem.name}</h2>
              <p>{selectedItem.itemPath}</p>
              <div className="detail-actions">
                <button type="button" onClick={() => openModal('folder')}>
                  Tạo Folder con
                </button>
                <button type="button" className="button-secondary" onClick={() => openModal('vocabSet')}>
                  Tạo VocabSet
                </button>
              </div>
            </section>
          ) : (
            <VocabSetDetail
              item={selectedItem}
              lastAddResult={lastAddResult}
              onOpenAddOne={() => openModal('addVocab')}
              onOpenBulkAdd={() => openModal('bulkAdd')}
            />
          )}
        </main>
      </div>

      {modal === 'folder' ? (
        <CreateFolderModal
          parentName={createParentName}
          submitting={submitting}
          errorMessage={modalError}
          onSubmit={handleCreateFolder}
          onClose={() => setModal(null)}
        />
      ) : null}
      {modal === 'vocabSet' ? (
        <CreateVocabSetModal
          parentName={createParentName}
          submitting={submitting}
          errorMessage={modalError}
          onSubmit={handleCreateVocabSet}
          onClose={() => setModal(null)}
        />
      ) : null}
      {modal === 'addVocab' && selectedVocabSet ? (
        <AddVocabModal
          vocabSetName={selectedVocabSet.name}
          submitting={submitting}
          errorMessage={modalError}
          onSubmit={handleAddVocab}
          onClose={() => setModal(null)}
        />
      ) : null}
      {modal === 'bulkAdd' && selectedVocabSet ? (
        <BulkAddVocabModal
          vocabSetName={selectedVocabSet.name}
          submitting={submitting}
          errorMessage={modalError}
          result={bulkResult}
          onSubmit={handleBulkAdd}
          onClose={() => setModal(null)}
        />
      ) : null}
    </section>
  )
}
