# AI Code Reviewer

Full-stack code reviewer that accepts snippets, forwards them to an LLM, stores the structured findings in MySQL, and renders interactive reports in a React UI.

## Stack
- **Backend:** Spring Boot 3, Spring Data JPA, Hibernate, MySQL
- **Frontend:** React (Vite + TypeScript), Axios, React Router
- **AI:** Pluggable HTTP client (defaults to OpenAI-style chat completions) with mock mode for local development

## Project layout
```
backend/   # Spring Boot service exposing /api/review endpoints
frontend/  # React SPA with submission form + history view
.env.example  # Shared environment variable template for backend + frontend
```

## Running locally
1. Copy `.env.example` to `.env` (or export variables another way) and fill in DB + AI credentials.
2. Start MySQL and create an empty database named `ai_code_reviewer` (or update `MYSQL_URL`).
3. **Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. **Frontend** (separate terminal)
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The UI is available at http://localhost:5173 and proxies `/api` calls to the backend.

> If you do not set `LLM_API_KEY` the backend stays in mock mode and returns deterministic sample reviews so you can exercise the UI without hitting an LLM.

## API surface
| Method | Route | Description |
| ------ | ----- | ----------- |
| `POST` | `/api/review` | Submit code, trigger AI review, persist + return structured response |
| `GET` | `/api/review` | Fetch up to 20 most recent reviews (id, language, score, timestamp) |
| `GET` | `/api/review/{id}` | Retrieve the stored code + full AI findings for a single review |

`Review` schema (auto-managed by JPA, shown here for clarity):
```sql
CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submitted_code LONGTEXT NOT NULL,
  language VARCHAR(64) NOT NULL,
  review_result LONGTEXT NOT NULL,
  quality_score INT,
  created_at DATETIME NOT NULL
);
```

## AI prompt template
The backend sends a predictable JSON-only prompt so the AI output is easy to parse:
```
Review the following <LANGUAGE> code. Identify concrete issues, offer actionable suggestions, rate the overall quality from 1-10, and list any best practices evidenced or missing.
Respond strictly with JSON matching this schema: {
  "issues": string[],
  "suggestions": string[],
  "qualityScore": number,
  "bestPractices": string[]
}

<CODE SNIPPET>
```
This template lives in `AiReviewService.PROMPT_TEMPLATE` and is the exact text forwarded to the configured model.

## Environment variables
Key settings pulled from the environment:
- `LLM_API_KEY` – secret used to authenticate with the selected provider
- `AI_MODEL`, `AI_BASE_URL`, `AI_PROVIDER`, `AI_SYSTEM_PROMPT`, `AI_TIMEOUT_SECONDS`, `AI_MOCK_MODE`
- `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `CORS_ALLOWED_ORIGINS`
- `VITE_API_BASE_URL` – frontend override for API origin (default `http://localhost:8080/api`)

## Frontend features
- Code submission form with language dropdown and validation
- Result panel highlighting issues, suggestions, quality score, and best practices with download buttons for Markdown/JSON
- History screen listing past reviews and their full details, including the original code sample for quick context

## Backend highlights
- Layered architecture (controller → service → repository)
- Dedicated `AiReviewService` that handles prompt construction, external HTTP calls, response parsing, and a deterministic mock fallback
- `Review` entity persisted in MySQL via Spring Data JPA with JSON payload storage
- Global exception handler returning consistent API error envelopes
- CORS configuration that can be tuned with `CORS_ALLOWED_ORIGINS`

## Example prompt + response handling
See `AiReviewService` for the complete request/response wiring. When `AI_MOCK_MODE=true` the service returns a predictable response, making it easy to wire up the UI before switching to a real LLM.
