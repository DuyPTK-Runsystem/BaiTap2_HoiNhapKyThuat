import { useCallback, useEffect, useState } from 'react'
import { StatusMessage } from '../../../components/StatusMessage'
import { LibraryTree } from '../../library/components/LibraryTree'
import { getChildren } from '../../../services/organizationService'
import type { Item } from '../../../types/organization'

interface VocabSourcePanelProps {
  accessToken: string | null
  selectedVocabSetId: number | null
  seeAll: boolean
  onSeeAllChange: (seeAll: boolean) => void
  onSelectVocabSet: (item: Item) => void
}

function errorMessageOf(error: unknown): string {
  return error instanceof Error ? error.message : 'Không thể tải Folder và VocabSet.'
}

export function VocabSourcePanel({
  accessToken,
  selectedVocabSetId,
  seeAll,
  onSeeAllChange,
  onSelectVocabSet,
}: VocabSourcePanelProps) {
  const [rootItems, setRootItems] = useState<Item[]>([])
  const [childrenByParentId, setChildrenByParentId] = useState<Record<number, Item[]>>({})
  const [expandedFolderIds, setExpandedFolderIds] = useState<Set<number>>(new Set())
  const [loadingFolderIds, setLoadingFolderIds] = useState<Set<number>>(new Set())
  const [loadingRoot, setLoadingRoot] = useState(true)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadRoot = useCallback(async () => {
    if (!accessToken) {
      return
    }

    setLoadingRoot(true)
    setErrorMessage(null)

    try {
      setRootItems(await getChildren(null, accessToken))
    } catch (error) {
      setErrorMessage(errorMessageOf(error))
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
        setErrorMessage(errorMessageOf(error))
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

  function handleToggleFolder(folder: Item) {
    if (seeAll) {
      return
    }

    const isExpanded = expandedFolderIds.has(folder.id)

    setExpandedFolderIds((current) => {
      const next = new Set(current)
      if (isExpanded) {
        next.delete(folder.id)
      } else {
        next.add(folder.id)
      }
      return next
    })

    if (!isExpanded && !childrenByParentId[folder.id]) {
      void loadFolderChildren(folder.id)
    }
  }

  function handleSelectItem(item: Item) {
    if (!seeAll && item.type === 'VOCAB_SET') {
      onSelectVocabSet(item)
    }
  }

  return (
    <aside className={seeAll ? 'vocab-source-panel is-disabled' : 'vocab-source-panel'}>
      <div className="vocab-source-header">
        <div>
          <p className="eyebrow">Source</p>
          <h3>Folders</h3>
        </div>
        <label className="vocab-see-all">
          <input type="checkbox" checked={seeAll} onChange={(event) => onSeeAllChange(event.target.checked)} />
          <span>See all vocabulary</span>
        </label>
      </div>
      {loadingRoot ? <StatusMessage tone="loading" title="Đang tải Folder" message="Đang lấy cây VocabSet." /> : null}
      {!loadingRoot && errorMessage ? <StatusMessage tone="error" title="Không thể tải source" message={errorMessage} /> : null}
      {!loadingRoot && !errorMessage ? (
        <LibraryTree
          rootItems={rootItems}
          childrenByParentId={childrenByParentId}
          expandedFolderIds={expandedFolderIds}
          selectedItemId={selectedVocabSetId}
          loadingFolderIds={loadingFolderIds}
          selectionDisabled={seeAll}
          onToggleFolder={handleToggleFolder}
          onSelectItem={handleSelectItem}
        />
      ) : null}
    </aside>
  )
}
