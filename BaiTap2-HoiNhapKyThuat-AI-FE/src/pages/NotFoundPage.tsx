import { Link } from 'react-router-dom'
import { StatusMessage } from '../components/StatusMessage'

export function NotFoundPage() {
  return (
    <main className="auth-layout">
      <div className="not-found-panel">
        <StatusMessage tone="error" title="Không tìm thấy trang" message="Đường dẫn này không nằm trong route map hiện tại." />
        <Link className="text-link" to="/">
          Về trang chính
        </Link>
      </div>
    </main>
  )
}
