import { useEffect, useState } from 'react'
import './App.css'
import { fetchConflicts, fetchFiles, fetchNodes, uploadFile } from './api'
import type { ConflictRecord, FileMetadata, NodeStatus, UploadResult } from './types'
import { NodeGrid } from './components/NodeGrid'
import { UploadPanel } from './components/UploadPanel'
import { FileTable } from './components/FileTable'
import { ConflictPanel } from './components/ConflictPanel'

type DashboardData = { nodes: NodeStatus[]; files: FileMetadata[] }

function App() {
  const [nodes, setNodes] = useState<NodeStatus[]>([])
  const [files, setFiles] = useState<FileMetadata[]>([])
  const [selectedFile, setSelectedFile] = useState<FileMetadata | undefined>()
  const [conflicts, setConflicts] = useState<ConflictRecord[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [toast, setToast] = useState('')

  useEffect(() => {
    loadData()
  }, [])

  async function loadData(): Promise<DashboardData | undefined> {
    setLoading(true)
    setError('')
    try {
      const [nodeData, fileData] = await Promise.all([fetchNodes(), fetchFiles()])
      setNodes(nodeData)
      setFiles(fileData)
      if (selectedFile) {
        const updated = fileData.find((file) => file.filename === selectedFile.filename)
        if (updated) {
          setSelectedFile(updated)
          refreshConflicts(updated.filename)
        }
      }
      return { nodes: nodeData, files: fileData }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load dashboard')
      return undefined
    } finally {
      setLoading(false)
    }
  }

  async function refreshConflicts(filename: string) {
    try {
      const data = await fetchConflicts(filename)
      setConflicts(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load conflicts')
    }
  }

  function handleSelect(file: FileMetadata) {
    setSelectedFile(file)
    refreshConflicts(file.filename)
  }

  async function handleUpload(file: File, options: Parameters<typeof uploadFile>[1]) {
    const result = await uploadFile(file, options)
    const data = await loadData()
    if (data) {
      const updated = data.files.find((item) => item.filename === result.filename)
      if (updated) {
        setSelectedFile(updated)
        refreshConflicts(updated.filename)
      }
    }
    return result
  }

  function handleUploadSuccess(result: UploadResult) {
    setToast(result.message)
    setTimeout(() => setToast(''), 4000)
  }

  return (
    <div className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">Distributed File Storage</p>
          <h1>Replication dashboard</h1>
          <p className="muted">
            Monitor node health, track replicas, and resolve conflicting writes from one place.
          </p>
        </div>
        <button onClick={loadData} disabled={loading}>
          {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      {error && <p className="error">{error}</p>}
      {toast && <div className="toast">{toast}</div>}

      <section>
        <h2>Nodes</h2>
        <NodeGrid nodes={nodes} />
      </section>

      <section className="layout-grid">
        <div>
          <h2>Files</h2>
          <FileTable files={files} onSelect={handleSelect} selected={selectedFile?.filename} />
        </div>
        <div>
          <h2>Conflicts</h2>
          <ConflictPanel file={selectedFile} conflicts={conflicts} />
        </div>
      </section>

      <section>
        <UploadPanel onUpload={handleUpload} onSuccess={handleUploadSuccess} />
      </section>
    </div>
  )
}

export default App
