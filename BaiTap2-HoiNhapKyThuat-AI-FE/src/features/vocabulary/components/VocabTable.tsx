import { useEffect, useState } from 'react'
import { fetchAudioBlob } from '../../../services/vocabularyService'
import type { Vocab } from '../../../types/vocabulary'

interface VocabTableProps {
  vocabs: Vocab[]
  accessToken: string | null
}

interface VocabAudioProps {
  audioPath: string | null
  accessToken: string | null
}

function VocabAudio({ audioPath, accessToken }: VocabAudioProps) {
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [loadedPath, setLoadedPath] = useState<string | null>(null)
  const [errorPath, setErrorPath] = useState<string | null>(null)

  useEffect(() => {
    if (!audioPath || !accessToken) {
      return
    }

    const controller = new AbortController()
    let objectUrl: string | null = null

    void fetchAudioBlob(audioPath, accessToken, controller.signal)
      .then((blob) => {
        objectUrl = URL.createObjectURL(blob)
        setAudioUrl(objectUrl)
        setLoadedPath(audioPath)
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }

        setErrorMessage('Audio không khả dụng.')
        setErrorPath(audioPath)
      })

    return () => {
      controller.abort()
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [accessToken, audioPath])

  if (!audioPath) {
    return <span>Chưa có audio</span>
  }

  if (errorPath === audioPath && errorMessage) {
    return <span>{errorMessage}</span>
  }

  return loadedPath === audioPath && audioUrl ? <audio controls preload="none" src={audioUrl} /> : <span>Đang tải audio...</span>
}

export function VocabTable({ vocabs, accessToken }: VocabTableProps) {
  return (
    <div className="vocab-table-wrapper">
      <table className="vocab-table">
        <thead>
          <tr>
            <th>Word</th>
            <th>Meaning</th>
            <th>IPA</th>
            <th>Audio</th>
            <th>Mastered</th>
          </tr>
        </thead>
        <tbody>
          {vocabs.map((vocab) => {
            return (
              <tr key={vocab.id}>
                <td className="vocab-word">{vocab.word}</td>
                <td>{vocab.meaning ?? 'Chưa có nghĩa'}</td>
                <td className="vocab-ipa">{vocab.ipa ?? 'Chưa có IPA'}</td>
                <td>
                  <VocabAudio audioPath={vocab.audioUrl ?? vocab.audio_url ?? null} accessToken={accessToken} />
                </td>
                <td>
                  <span className={vocab.mastered ? 'vocab-status is-mastered' : 'vocab-status'}>
                    {vocab.mastered ? 'Mastered' : 'Learning'}
                  </span>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
