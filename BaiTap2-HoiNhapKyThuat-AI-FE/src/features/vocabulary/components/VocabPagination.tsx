interface VocabPaginationProps {
  page: number
  pageSize: number
  totalPages: number
  totalItems: number
  loading: boolean
  onPageChange: (page: number) => void
  onPageSizeChange: (pageSize: number) => void
}

export function VocabPagination({
  page,
  pageSize,
  totalPages,
  totalItems,
  loading,
  onPageChange,
  onPageSizeChange,
}: VocabPaginationProps) {
  const safeTotalPages = Math.max(totalPages, 1)
  const startItem = totalItems === 0 ? 0 : (page - 1) * pageSize + 1
  const endItem = Math.min(page * pageSize, totalItems)

  return (
    <div className="vocab-pagination">
      <label>
        <span>Rows per page</span>
        <select
          value={pageSize}
          disabled={loading}
          onChange={(event) => onPageSizeChange(Number(event.target.value))}
        >
          <option value={10}>10</option>
          <option value={20}>20</option>
          <option value={50}>50</option>
        </select>
      </label>
      <span>
        {startItem}-{endItem} of {totalItems}
      </span>
      <div className="vocab-pagination-actions">
        <button type="button" disabled={loading || page <= 1} onClick={() => onPageChange(page - 1)}>
          Trước
        </button>
        <span>
          Trang {page} / {safeTotalPages}
        </span>
        <button type="button" disabled={loading || page >= safeTotalPages} onClick={() => onPageChange(page + 1)}>
          Sau
        </button>
      </div>
    </div>
  )
}
