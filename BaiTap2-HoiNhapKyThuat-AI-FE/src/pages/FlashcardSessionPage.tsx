import { StatusMessage } from '../components/StatusMessage'

export function FlashcardSessionPage() {
  return (
    <section className="route-page">
      <StatusMessage
        tone="empty"
        title="Flashcard Session"
        message="Màn hình flashcard sẽ hiển thị mặt trước theo meaning/audio và lật thẻ cục bộ."
      />
    </section>
  )
}
