# Distributed File Storage System

A reference implementation of a distributed file storage platform with a Java (Spring Boot) backend, MongoDB metadata store, pluggable storage tiers (local disk or S3-compatible), and a React dashboard for node/replica visibility.

## Architecture

- **Metadata service (Spring Boot)** — exposes REST APIs for file upload/download, node status, and conflict inspection. Metadata is persisted in MongoDB (`file_metadata` collection).
- **Replication engine** — copies each upload to the requested number of simulated storage nodes. Nodes default to local folders (`backend/storage/node-*`) but an S3-compatible backend can be enabled via configuration.
- **Conflict handling** — optimistic version checks with selectable strategies: `FAIL_FAST`, `LAST_WRITE_WINS`, or `KEEP_BOTH` (forks to a timestamped filename and logs the conflict trail).
- **React dashboard** — monitors node health, replica placement, and outstanding conflicts. Includes an upload form that exercises the REST APIs.

## Backend (Spring Boot)

### Prerequisites

- Java 17+
- Maven 3.9+
- MongoDB running locally (default URI: `mongodb://localhost:27017/distributed-file-storage`)

### Run the service

```bash
cd backend
mvn spring-boot:run
```

Key endpoints (default port `8080`):

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/files` | Multipart upload with replication + conflict strategy |
| `GET` | `/api/files` | List file metadata, versions, replicas |
| `GET` | `/api/files/{filename}` | Download latest (or `?version=n`) |
| `GET` | `/api/files/{filename}/conflicts` | Conflict history |
| `GET` | `/api/nodes` | Node heartbeat + capacity stats |

Configuration lives in `backend/src/main/resources/application.yml`. Toggle storage mode:

```yaml
dfs:
  storage:
    mode: S3 # or LOCAL
    localBasePath: storage
    s3:
      endpoint: http://localhost:9000
      region: us-east-1
      bucket: dfs-files
      accessKey: minio
      secretKey: minio123
```

The default setup registers three local nodes (`node-a` → `node-c`). Storage folders are created automatically under `backend/storage` when the service boots.

## Frontend (React + Vite)

### Prerequisites

- Node.js 20+
- npm 10+

### Run the dashboard

```bash
cd frontend
npm install # already run during setup
npm run dev
```

Vite proxies `/api/**` calls to `http://localhost:8080` during development (configurable via `VITE_API_BASE`). The dashboard exposes:

- **Node grid** — heartbeat + capacity visualization per node.
- **File table** — metadata, replica placements, conflict counts.
- **Conflict panel** — drill-down for the selected file.
- **Upload panel** — choose replication factor + conflict strategy to exercise the backend.

### Production build

```bash
cd frontend
npm run build
npm run preview
```

## Testing

Backend smoke test:

```bash
cd backend
mvn test
```

## Next steps

- Wire S3-compatible storage (e.g., MinIO) via the provided configuration.
- Deploy MongoDB + the Spring Boot service behind an API gateway and ship the React build via a CDN.
