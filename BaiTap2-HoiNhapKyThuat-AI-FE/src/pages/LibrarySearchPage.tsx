import { useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'
import { LibrarySearchResults } from '../features/library/components/LibrarySearchResults'
import { getItemByPath, searchItems } from '../services/organizationService'
import type { Item } from '../types/organization'
import type { FormEvent } from 'react'

interface ProtectedOutletContext {
  accessToken: string | null
}

function errorMessageOf(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback
}

export function LibrarySearchPage() {
  const { accessToken } = useOutletContext<ProtectedOutletContext>()
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [items, setItems] = useState<Item[]>([])
  const [searched, setSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const trimmedKeyword = keyword.trim()

    if (!accessToken || !trimmedKeyword || loading) {
      return
    }

    setLoading(true)
    setErrorMessage(null)
    setSearched(true)

    try {
      const result = await searchItems(trimmedKeyword, accessToken)
      setItems(result)
    } catch (error) {
      setItems([])
      setErrorMessage(errorMessageOf(error, 'Không thể tìm kiếm item.'))
    } finally {
      setLoading(false)
    }
  }

  async function handleOpenItem(item: Item) {
    if (!accessToken || loading) {
      return
    }

    setLoading(true)
    setErrorMessage(null)

    try {
      const resolvedItem = await getItemByPath(item.itemPath, accessToken)
      navigate('/library', { state: { selectedItem: resolvedItem } })
    } catch (error) {
      setErrorMessage(errorMessageOf(error, 'Không thể mở item theo path.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="library-page">
      <div className="library-toolbar">
        <div>
          <p className="eyebrow">Organization</p>
          <h2>Library Search</h2>
          <p>Tìm Folder hoặc VocabSet theo tên và mở item bằng itemPath.</p>
        </div>
      </div>

      <form className="library-search-form" onSubmit={handleSearch}>
        <input
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="Nhập tên Folder hoặc VocabSet"
          disabled={loading}
        />
        <button type="submit" disabled={!keyword.trim() || loading}>
          {loading ? 'Đang tìm' : 'Tìm kiếm'}
        </button>
      </form>

      {errorMessage ? <StatusMessage tone="error" title="Search error" message={errorMessage} /> : null}
      {loading ? <StatusMessage tone="loading" title="Đang xử lý" message="Đang gọi API Organization." /> : null}
      {!loading && searched && !errorMessage ? <LibrarySearchResults items={items} onOpenItem={handleOpenItem} /> : null}
      {!searched ? <StatusMessage tone="empty" title="Chưa tìm kiếm" message="Nhập từ khóa để tìm item theo contains/LIKE." /> : null}
    </section>
  )
}
