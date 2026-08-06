import type { Item } from '../../../types/organization'
import type { AddVocabToSetResponse } from '../../../types/vocabulary'

interface VocabSetDetailProps {
  item: Item
  lastAddResult: AddVocabToSetResponse | null
  onOpenAddOne: () => void
  onOpenBulkAdd: () => void
}

export function VocabSetDetail({ item, lastAddResult, onOpenAddOne, onOpenBulkAdd }: VocabSetDetailProps) {
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
      {lastAddResult ? (
        <p className="form-success">
          Vocab #{lastAddResult.vocab.id} {lastAddResult.added ? 'đã được thêm vào' : 'đã tồn tại trong'} {lastAddResult.vocabSet.name}.
        </p>
      ) : null}
      <p className="library-muted">API hiện chưa công bố endpoint lấy danh sách vocab trong VocabSet, nên màn hình chỉ hiển thị summary và thao tác add.</p>
    </section>
  )
}
