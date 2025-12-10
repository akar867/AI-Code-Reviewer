export type NodeStatus = {
  id: string
  name: string
  healthy: boolean
  capacityGb: number
  usedBytes: number
  usagePercent: number
  lastHeartbeat: string
}

export type FileVersion = {
  version: number
  createdAt: string
  checksum: string
  size: number
  replicas: string[]
}

export type FileMetadata = {
  filename: string
  contentType: string
  currentVersion: number
  updatedAt: string
  versions: FileVersion[]
  conflicts: string[]
}

export type ConflictRecord = {
  expectedVersion: number
  actualVersion: number
  actor: string
  strategy: string
  detectedAt: string
  note: string
}

export type UploadResult = {
  filename: string
  version: number
  checksum: string
  message: string
}

export const ConflictStrategy = {
  FailFast: 'FAIL_FAST',
  LastWriteWins: 'LAST_WRITE_WINS',
  KeepBoth: 'KEEP_BOTH',
} as const

export type ConflictStrategy = (typeof ConflictStrategy)[keyof typeof ConflictStrategy]
