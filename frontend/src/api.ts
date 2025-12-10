import type {
  ConflictRecord,
  ConflictStrategy,
  FileMetadata,
  NodeStatus,
  UploadResult,
} from './types'

const API_BASE = import.meta.env.VITE_API_BASE || ''

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, init)
  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Request failed')
  }
  return response.json() as Promise<T>
}

export function fetchNodes(): Promise<NodeStatus[]> {
  return request<NodeStatus[]>('/api/nodes')
}

export function fetchFiles(): Promise<FileMetadata[]> {
  return request<FileMetadata[]>('/api/files')
}

export function fetchConflicts(filename: string): Promise<ConflictRecord[]> {
  return request<ConflictRecord[]>(`/api/files/${encodeURIComponent(filename)}/conflicts`)
}

export async function uploadFile(
  file: File,
  options: {
    owner?: string
    replicationFactor: number
    expectedVersion?: number | null
    strategy: ConflictStrategy
  },
): Promise<UploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  if (options.owner) {
    formData.append('owner', options.owner)
  }
  formData.append('replicationFactor', `${options.replicationFactor}`)
  if (options.expectedVersion !== undefined && options.expectedVersion !== null) {
    formData.append('expectedVersion', `${options.expectedVersion}`)
  }
  formData.append('strategy', options.strategy)
  const response = await fetch(`${API_BASE}/api/files`, {
    method: 'POST',
    body: formData,
  })
  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Upload failed')
  }
  return response.json() as Promise<UploadResult>
}
