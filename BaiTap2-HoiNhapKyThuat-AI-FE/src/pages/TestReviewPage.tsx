import { useParams } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'

export function TestReviewPage() {
  const { testId } = useParams()

  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title={`Test ${testId ?? ''} Review`.trim()}
        message="Màn hình review sẽ hiển thị câu hỏi, đáp án đã chọn, đáp án đúng và audio nếu response có URL."
      />
    </section>
  )
}
