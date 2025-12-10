import type { FileMetadata } from '../types'

interface Props {
  files: FileMetadata[]
  onSelect: (file: FileMetadata) => void
  selected?: string
}

export function FileTable({ files, onSelect, selected }: Props) {
  if (!files.length) {
    return <p className="muted">No files have been uploaded yet.</p>
  }
  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Filename</th>
            <th>Version</th>
            <th>Replicas</th>
            <th>Updated</th>
            <th>Conflicts</th>
          </tr>
        </thead>
        <tbody>
          {files.map((file) => (
            <tr
              key={file.filename}
              className={selected === file.filename ? 'selected' : ''}
              onClick={() => onSelect(file)}
            >
              <td>{file.filename}</td>
              <td>{file.currentVersion}</td>
              <td>{file.versions[file.versions.length - 1]?.replicas.join(', ')}</td>
              <td>{new Date(file.updatedAt).toLocaleString()}</td>
              <td>{file.conflicts.length}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
