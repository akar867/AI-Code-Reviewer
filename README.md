# AI Code Reviewer

LLM-powered reviewer that ingests GitHub pull request webhooks, parses diffs, generates structured findings, missing tests, and a risk score, then surfaces the report in a React dashboard and GitHub comments.

## Architecture
- **Backend (`backend/`)** – Spring Boot service that exposes REST APIs, processes GitHub webhooks, parses diffs, orchestrates LLM calls, persists structured data in MySQL and raw diffs/logs in MongoDB.
- **Frontend (`frontend/`)** – React + Tailwind dashboard built with Vite that visualizes review summaries, findings, and auto-generated test ideas in real time.
- **Datastores** – MySQL for PR metadata, findings, and risk scoring; MongoDB for raw diffs + telemetry.
- **LLM Providers** – OpenAI or Anthropic pluggable via configuration with deterministic heuristics fallback when API keys are missing.

```
GitHub Webhook -> Spring Boot WebhookController
  -> DiffParser -> PromptBuilder -> LlmGateway
  -> RiskScoringService -> Persist (MySQL + Mongo)
  -> NotificationService posts GitHub summary
  -> React dashboard consumes /api/reviews endpoints
```

## Key Backend Components
| Area | Highlights |
| ---- | ---------- |
| Webhooks | `WebhookController` validates GitHub signatures, filters PR actions, downloads diffs, and hands off to `ReviewService`. |
| Diff understanding | `DiffParser` converts unified diffs into structured files/hunks/line changes for downstream reasoning and heuristics. |
| LLM orchestration | `PromptBuilder` crafts review/test prompts; `LlmGateway` picks OpenAI/Anthropic, requests JSON output, and falls back to heuristics if necessary. |
| Risk scoring | `RiskScoringService` blends LLM risk hints, finding severity, and patch footprint to produce a 0-100 score + LOW/MEDIUM/HIGH badge. |
| Persistence | Relational entities (`PullRequestReview`, `ReviewFinding`, `TestCaseSuggestion`) live in MySQL; `DiffDocument` stores raw patch + metadata in Mongo. |
| Notifications | `NotificationService` can auto-post condensed findings back to the originating PR. |

## Frontend Dashboard
- Modern React SPA with React Query data fetching and Tailwind UI.
- Shows PR list with filters, risk chip, human-readable summary, findings list, and test suggestions.
- Configurable API origin via `VITE_API_URL`.

## Getting Started
```bash
# spin up databases
docker compose up -d

# backend
cd backend
mvn spring-boot:run

# frontend
cd frontend
npm install
npm run dev
```

Expose the backend via `https://<host>/api/webhooks/github` and register that URL with your GitHub App/webhook, supplying the same secret as `GITHUB_WEBHOOK_SECRET`.

## Configuration
Copy `.env.example` to `.env` (or export env vars) and set:

| Variable | Purpose |
| -------- | ------- |
| `MYSQL_*`, `MONGODB_URI` | Database credentials |
| `GITHUB_TOKEN` | PAT with `repo` scope to fetch diffs & post comments |
| `GITHUB_WEBHOOK_SECRET` | Shared secret for webhook signature checks |
| `OPENAI_API_KEY` / `ANTHROPIC_API_KEY` | Provider credentials |
| `LLM_PROVIDER` | `openai` (default) or `anthropic` |
| `VITE_API_URL` | Backend origin (e.g., `http://localhost:8080`, no `/api`) |

## Testing Webhooks Locally
1. Expose port 8080 via `ngrok http 8080`.
2. Configure the GitHub App/Webhook to target `https://<ngrok>/api/webhooks/github`.
3. Trigger a PR action (open/synchronize). The backend will download the diff, call the LLM/heuristics, store the review, and optionally comment on the PR.
4. Open `http://localhost:5173` to view the dashboard.

## Next Ideas
- Add background job queue (e.g., RabbitMQ) for large diffs.
- Persist conversation transcripts for observability.
- Expand heuristics for language-specific rules and AST parsing.
- Ship per-finding GitHub review comments and suggested patches.
