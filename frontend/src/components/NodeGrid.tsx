import type { NodeStatus } from '../types'

export function NodeGrid({ nodes }: { nodes: NodeStatus[] }) {
  if (!nodes.length) {
    return <p className="muted">No nodes registered</p>
  }
  return (
    <div className="node-grid">
      {nodes.map((node) => (
        <article key={node.id} className="card">
          <header className="card-header">
            <div>
              <strong>{node.name}</strong>
              <p className="muted">{node.id}</p>
            </div>
            <span className={node.healthy ? 'status status-ok' : 'status status-bad'}>
              {node.healthy ? 'Healthy' : 'Offline'}
            </span>
          </header>
          <section className="card-body">
            <p>
              Usage: {node.usagePercent.toFixed(1)}% ({formatBytes(node.usedBytes)} / {node.capacityGb} GB)
            </p>
            <progress value={node.usagePercent} max={100} />
            <p className="muted">Last heartbeat: {new Date(node.lastHeartbeat).toLocaleString()}</p>
          </section>
        </article>
      ))}
    </div>
  )
}

function formatBytes(bytes: number) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const idx = Math.floor(Math.log(bytes) / Math.log(1024))
  const value = bytes / Math.pow(1024, idx)
  return `${value.toFixed(1)} ${units[idx]}`
}
