import type { Item } from '../../../types/organization'
import type { CreateVocabInSetResponse } from '../../../types/vocabulary'

interface VocabSetDetailProps {
  item: Item
  lastAddResult: CreateVocabInSetResponse | null
  onOpenAddOne: () => void
  onOpenBulkAdd: () => void
}

export function VocabSetDetail({ item, lastAddResult, onOpenAddOne, onOpenBulkAdd }: VocabSetDetailProps) {
  const visibleAddResult = lastAddResult?.vocabSet.id === item.id ? lastAddResult : null

  return (
    <section className="library-detail-card">
      <div>
        <p className="eyebrow">VocabSet</p>
        <h2>{item.name}</h2>
        <p>{item.description || 'Chưa có mô tả.'}</p>
      </div>
      <dl className="detail-stats">
        <div>
          <dt>Vocab count</dt>
          <dd>{item.vocabCount ?? 0}</dd>
        </div>
        <div>
          <dt>Path</dt>
          <dd>{item.itemPath}</dd>
        </div>
      </dl>
      <div className="detail-actions">
        <button type="button" onClick={onOpenAddOne}>
          Add Vocab
        </button>
        <button type="button" className="button-secondary" onClick={onOpenBulkAdd}>
          Bulk Add
        </button>
      </div>
      {visibleAddResult ? (
        <p className="form-success">
          Đã tạo Vocab "{visibleAddResult.vocab.word}" {visibleAddResult.added ? 'và gắn vào' : 'nhưng chưa gắn thêm vào'}{' '}
          {visibleAddResult.vocabSet.name}.
        </p>
      ) : null}
      <p className="library-muted">API hiện chưa công bố endpoint lấy danh sách vocab trong VocabSet, nên màn hình chỉ hiển thị summary và thao tác add.</p>
    </section>
  )
}
