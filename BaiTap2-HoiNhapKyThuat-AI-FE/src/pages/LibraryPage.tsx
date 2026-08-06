import { StatusMessage } from '../components/StatusMessage'

export function LibraryPage() {
  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title="Vocabulary Library"
        message="Màn hình thư viện từ vựng sẽ hiển thị cây Folder/VocabSet và nội dung đang chọn."
      />
    </section>
  )
}
