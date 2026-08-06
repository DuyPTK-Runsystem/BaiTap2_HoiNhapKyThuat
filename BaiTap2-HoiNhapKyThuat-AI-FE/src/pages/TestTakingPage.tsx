import { useParams } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'

export function TestTakingPage() {
  const { testId } = useParams()

  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title={`Test ${testId ?? ''}`.trim()}
        message="Màn hình làm bài sẽ tải test, hiển thị câu hỏi, lựa chọn và bộ đếm thời gian nếu có."
      />
    </section>
  )
}
