import type { Item } from '../../../types/organization'

interface LibrarySearchResultsProps {
  items: Item[]
  onOpenItem: (item: Item) => void
}

export function LibrarySearchResults({ items, onOpenItem }: LibrarySearchResultsProps) {
  if (items.length === 0) {
    return <p className="library-muted">Không có kết quả phù hợp.</p>
  }

  return (
    <div className="library-table-wrap">
      <table className="library-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Path</th>
            <th>Vocab</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              <td>
                <strong>{item.name}</strong>
                <span>{item.description || 'No description'}</span>
              </td>
              <td>{item.type}</td>
              <td>{item.itemPath}</td>
              <td>{item.vocabCount ?? '-'}</td>
              <td>
                <button type="button" className="button-secondary" onClick={() => onOpenItem(item)}>
                  Open
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
