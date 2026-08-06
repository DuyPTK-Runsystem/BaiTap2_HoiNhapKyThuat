import { StatusMessage } from '../components/StatusMessage'

export function StudyCreatePage() {
  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title="Create Study Session"
        message="Màn hình tạo bài test hoặc flashcard sẽ cho chọn nguồn Folder/VocabSet và số lượng câu hỏi/thẻ."
      />
    </section>
  )
}
