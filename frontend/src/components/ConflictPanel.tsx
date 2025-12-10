import type { ConflictRecord, FileMetadata } from '../types'

interface Props {
  file?: FileMetadata
  conflicts: ConflictRecord[]
}

export function ConflictPanel({ file, conflicts }: Props) {
  if (!file) {
    return <p className="muted">Select a file to inspect conflicts.</p>
  }
  return (
    <div className="card">
      <header className="card-header">
        <strong>Conflicts for {file.filename}</strong>
      </header>
      {conflicts.length === 0 ? (
        <section className="card-body">
          <p>No conflicts recorded.</p>
        </section>
      ) : (
        <section className="card-body conflict-list">
          {conflicts.map((conflict) => (
            <article key={`${conflict.detectedAt}-${conflict.expectedVersion}`} className="conflict-item">
              <div>
                <p>
                  expected {conflict.expectedVersion} • actual {conflict.actualVersion} •{' '}
                  {conflict.strategy}
                </p>
                <p className="muted">{new Date(conflict.detectedAt).toLocaleString()}</p>
              </div>
              <p className="muted">{conflict.note}</p>
            </article>
          ))}
        </section>
      )}
    </div>
  )
}
