import { useParams } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'

export function TestResultPage() {
  const { testId } = useParams()

  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title={`Test ${testId ?? ''} Result`.trim()}
        message="Màn hình kết quả sẽ hiển thị số câu đúng/sai và trạng thái hoàn thành của bài test."
      />
    </section>
  )
}
