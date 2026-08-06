import { StatusMessage } from '../components/StatusMessage'

export function LibrarySearchPage() {
  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title="Library Search"
        message="Màn hình tìm kiếm sẽ dùng endpoint search items theo tên và hiển thị kết quả thuộc người dùng hiện tại."
      />
    </section>
  )
}
