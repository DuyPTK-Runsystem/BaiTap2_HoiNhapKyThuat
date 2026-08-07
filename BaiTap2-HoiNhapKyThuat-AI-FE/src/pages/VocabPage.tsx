import { useCallback, useEffect, useState } from 'react'
import { useOutletContext, useSearchParams } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'
import { VocabPagination } from '../features/vocabulary/components/VocabPagination'
import { VocabSourcePanel } from '../features/vocabulary/components/VocabSourcePanel'
import { VocabTable } from '../features/vocabulary/components/VocabTable'
import { getVocabs } from '../services/vocabularyService'
import type { Item } from '../types/organization'
import type { PaginatedVocabs } from '../types/vocabulary'

interface ProtectedOutletContext {
  accessToken: string | null
}

const defaultPageSize = 10

function errorMessageOf(error: unknown): string {
  return error instanceof Error ? error.message : 'Không thể tải danh sách Vocab.'
}

function parseVocabSetId(value: string | null): number | null {
  if (!value) {
    return null
  }

  const id = Number(value)
  return Number.isInteger(id) && id > 0 ? id : null
}

export function VocabPage() {
  const { accessToken } = useOutletContext<ProtectedOutletContext>()
  const [searchParams, setSearchParams] = useSearchParams()
  const selectedVocabSetId = parseVocabSetId(searchParams.get('vocabSetId'))
  const [seeAll, setSeeAll] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(defaultPageSize)
  const [data, setData] = useState<PaginatedVocabs | null>(null)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const loadVocabs = useCallback(async () => {
    if (!accessToken || (!seeAll && selectedVocabSetId === null)) {
      setData(null)
      setLoading(false)
      return
    }

    setLoading(true)
    setErrorMessage(null)

    try {
      const result = await getVocabs(seeAll ? undefined : selectedVocabSetId ?? undefined, page, pageSize, accessToken)
      setData(result)
    } catch (error) {
      setErrorMessage(errorMessageOf(error))
    } finally {
      setLoading(false)
    }
  }, [accessToken, page, pageSize, seeAll, selectedVocabSetId])

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void loadVocabs()
    }, 0)

    return () => window.clearTimeout(timeoutId)
  }, [loadVocabs])

  function handleSeeAllChange(nextSeeAll: boolean) {
    setSeeAll(nextSeeAll)
    setPage(1)
  }

  function handleSelectVocabSet(item: Item) {
    setSearchParams({ vocabSetId: String(item.id) })
    setSeeAll(false)
    setPage(1)
  }

  function handlePageSizeChange(nextPageSize: number) {
    setPageSize(nextPageSize)
    setPage(1)
  }

  const vocabs = data?.result ?? []
  const meta = data?.meta ?? { page, pageSize, totalPages: 0, totalItems: 0 }
  const hasSource = seeAll || selectedVocabSetId !== null

  return (
    <section className="vocab-page">
      <div className="vocab-page-header">
        <div>
          <p className="eyebrow">Vocabulary</p>
          <h2>Vocabulary</h2>
          <p>{seeAll ? 'Tất cả từ vựng trong hệ thống.' : 'Danh sách từ vựng trong VocabSet.'}</p>
        </div>
      </div>

      <div className="vocab-layout">
        <VocabSourcePanel
          accessToken={accessToken}
          selectedVocabSetId={selectedVocabSetId}
          seeAll={seeAll}
          onSeeAllChange={handleSeeAllChange}
          onSelectVocabSet={handleSelectVocabSet}
        />
        <section className="vocab-content-card">
          {!hasSource ? (
            <StatusMessage
              tone="empty"
              title="Chưa chọn VocabSet"
              message="Chọn một VocabSet ở panel bên trái hoặc bật See all vocabulary."
            />
          ) : null}
          {hasSource && loading ? (
            <StatusMessage tone="loading" title="Đang tải Vocab" message="Đang lấy danh sách từ vựng." />
          ) : null}
          {hasSource && !loading && errorMessage ? (
            <div className="vocab-error-state">
              <StatusMessage tone="error" title="Không thể tải Vocab" message={errorMessage} />
              <button type="button" onClick={() => void loadVocabs()}>
                Thử lại
              </button>
            </div>
          ) : null}
          {hasSource && !loading && !errorMessage && vocabs.length === 0 ? (
            <StatusMessage tone="empty" title="Chưa có Vocab" message="Không tìm thấy từ vựng phù hợp." />
          ) : null}
          {hasSource && !loading && !errorMessage && vocabs.length > 0 ? (
            <VocabTable vocabs={vocabs} accessToken={accessToken} />
          ) : null}
          {hasSource && !errorMessage ? (
            <VocabPagination
              page={meta.page}
              pageSize={meta.pageSize}
              totalPages={meta.totalPages}
              totalItems={meta.totalItems}
              loading={loading}
              onPageChange={setPage}
              onPageSizeChange={handlePageSizeChange}
            />
          ) : null}
        </section>
      </div>
    </section>
  )
}
