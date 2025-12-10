import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { ConflictStrategy } from '../types'
import type { ConflictStrategy as ConflictStrategyValue, UploadResult } from '../types'

interface Props {
  onUpload: (file: File, options: {
    owner?: string
    replicationFactor: number
    expectedVersion?: number | null
    strategy: ConflictStrategyValue
  }) => Promise<UploadResult>
  onSuccess: (result: UploadResult) => void
}

export function UploadPanel({ onUpload, onSuccess }: Props) {
  const [file, setFile] = useState<File | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [owner, setOwner] = useState('')
  const [replicationFactor, setReplicationFactor] = useState(2)
  const [expectedVersion, setExpectedVersion] = useState<string>('')
  const [strategy, setStrategy] = useState<ConflictStrategyValue>(ConflictStrategy.LastWriteWins)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<string>('')

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!file) {
      setError('Select a file first')
      return
    }
    setPending(true)
    setError('')
    try {
      const result = await onUpload(file, {
        owner: owner || undefined,
        replicationFactor,
        expectedVersion: expectedVersion ? Number(expectedVersion) : undefined,
        strategy,
      })
      onSuccess(result)
      setFile(null)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
      setOwner('')
      setExpectedVersion('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed')
    } finally {
      setPending(false)
    }
  }

  return (
    <form className="card upload-panel" onSubmit={handleSubmit}>
      <header className="card-header">
        <strong>Upload file</strong>
      </header>
      <section className="card-body upload-grid">
        <label>
          <span>File</span>
          <input ref={fileInputRef} type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        </label>
        <label>
          <span>Owner</span>
          <input value={owner} onChange={(e) => setOwner(e.target.value)} placeholder="ops@acme.io" />
        </label>
        <label>
          <span>Replication factor</span>
          <input
            type="number"
            min={1}
            max={5}
            value={replicationFactor}
            onChange={(e) => setReplicationFactor(Number(e.target.value))}
          />
        </label>
        <label>
          <span>Expected version</span>
          <input
            type="number"
            min={0}
            placeholder="optional"
            value={expectedVersion}
            onChange={(e) => setExpectedVersion(e.target.value)}
          />
        </label>
        <label>
          <span>Conflict strategy</span>
          <select value={strategy} onChange={(e) => setStrategy(e.target.value as ConflictStrategyValue)}>
            <option value={ConflictStrategy.LastWriteWins}>Last write wins</option>
            <option value={ConflictStrategy.FailFast}>Fail fast</option>
            <option value={ConflictStrategy.KeepBoth}>Keep both</option>
          </select>
        </label>
      </section>
      {error && <p className="error">{error}</p>}
      <footer className="card-footer">
        <button type="submit" disabled={pending}>
          {pending ? 'Uploading…' : 'Upload'}
        </button>
      </footer>
    </form>
  )
}
