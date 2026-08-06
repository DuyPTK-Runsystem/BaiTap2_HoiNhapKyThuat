import { StatusMessage } from '../components/StatusMessage'

const foundationModules = [
  {
    title: 'API client',
    description: 'Response wrapper, Bearer token, JSON request và FormData import đã có nền dùng chung.',
  },
  {
    title: 'Domain services',
    description: 'Auth, Organization, Vocabulary, Testing và Flashcard được tách service theo contract.',
  },
  {
    title: 'UI shell',
    description: 'Sidebar, topbar và workspace placeholder theo hướng thiết kế Vocab Library.',
  },
] as const

export function DashboardPage() {
  return (
    <div className="dashboard-page">
      <StatusMessage
        tone="success"
        title="Foundation đã sẵn sàng để mở rộng"
        message="Các module tiếp theo có thể dùng lại API client, service, type và app shell này."
      />

      <section className="module-grid" aria-label="Nền tảng đã dựng">
        {foundationModules.map((module) => (
          <article className="module-card" key={module.title}>
            <span>{module.title}</span>
            <p>{module.description}</p>
          </article>
        ))}
      </section>
    </div>
  )
}
