import type { Item } from '../../../types/organization'

interface LibraryTreeProps {
  rootItems: Item[]
  childrenByParentId: Record<number, Item[]>
  expandedFolderIds: Set<number>
  selectedItemId: number | null
  loadingFolderIds: Set<number>
  onToggleFolder: (folder: Item) => void
  onSelectItem: (item: Item) => void
}

function itemMeta(item: Item): string {
  if (item.type === 'FOLDER') {
    return 'Folder'
  }

  return `${item.vocabCount ?? 0} vocab`
}

interface TreeBranchProps extends Omit<LibraryTreeProps, 'rootItems'> {
  items: Item[]
  depth: number
  ancestorIds: Set<number>
}

function TreeBranch({
  items,
  depth,
  childrenByParentId,
  expandedFolderIds,
  selectedItemId,
  loadingFolderIds,
  onToggleFolder,
  onSelectItem,
  ancestorIds,
}: TreeBranchProps) {
  if (items.length === 0) {
    return null
  }

  return (
    <ul className="library-tree-list">
      {items.map((item) => {
        const hasCycle = ancestorIds.has(item.id)
        const expanded = expandedFolderIds.has(item.id)
        const loading = loadingFolderIds.has(item.id)
        const children = childrenByParentId[item.id] ?? []
        const nextAncestorIds = new Set(ancestorIds).add(item.id)

        return (
          <li key={item.id}>
            <div className="library-tree-row" style={{ paddingLeft: 12 + depth * 18 }}>
              {item.type === 'FOLDER' ? (
                <button
                  className="tree-toggle"
                  type="button"
                  onClick={() => onToggleFolder(item)}
                  aria-label={expanded ? 'Thu gọn folder' : 'Mở rộng folder'}
                >
                  {loading ? '...' : expanded ? 'v' : '>'}
                </button>
              ) : (
                <span className="tree-spacer" aria-hidden="true" />
              )}
              <button
                className={selectedItemId === item.id ? 'tree-item is-selected' : 'tree-item'}
                type="button"
                onClick={() => onSelectItem(item)}
              >
                <strong>{item.name}</strong>
                <span>{itemMeta(item)}</span>
              </button>
            </div>
            {expanded && !hasCycle ? (
              <TreeBranch
                items={children}
                depth={depth + 1}
                ancestorIds={nextAncestorIds}
                childrenByParentId={childrenByParentId}
                expandedFolderIds={expandedFolderIds}
                selectedItemId={selectedItemId}
                loadingFolderIds={loadingFolderIds}
                onToggleFolder={onToggleFolder}
                onSelectItem={onSelectItem}
              />
            ) : null}
          </li>
        )
      })}
    </ul>
  )
}

export function LibraryTree(props: LibraryTreeProps) {
  if (props.rootItems.length === 0) {
    return <p className="library-muted">Chưa có Folder hoặc VocabSet ở root.</p>
  }

  return <TreeBranch {...props} items={props.rootItems} depth={0} ancestorIds={new Set()} />
}
