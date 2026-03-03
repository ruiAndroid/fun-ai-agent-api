# fun-ai-agent-api

Unified API gateway for the agent platform.

## Tech stack

- Java 17
- Spring Boot 3 (WebFlux)

## Run locally

```bash
mvn spring-boot:run
```

Default config:

- API port: `8080`
- Plane base url: `http://localhost:8100`

Environment variables:

- `PLANE_BASE_URL`
- `PLANE_TIMEOUT_SECONDS`

## Endpoints

- `POST /api/v1/tasks` (compatible: `/v1/tasks`)
- `GET /api/v1/tasks/{taskId}` (compatible: `/v1/tasks/{taskId}`)
- `POST /api/v1/tasks/{taskId}/cancel` (compatible: `/v1/tasks/{taskId}/cancel`)
- `GET /api/v1/tasks/{taskId}/events` (SSE, compatible: `/v1/tasks/{taskId}/events`)

## Create task payload

Gateway accepts both `snake_case` and `camelCase` for these fields:

- `tenant_id` / `tenantId`
- `agent_id` / `agentId`
- `workflow_id` / `workflowId` (optional)
- `skill_id` / `skillId` (optional)
- `skill_prompt_override` / `skillPromptOverride` (optional)
- `skill_prompt_overrides` / `skillPromptOverrides` (optional)
- `prompt`
- `idempotency_key` / `idempotencyKey` (optional)

Gateway forwards validated fields to plane as `snake_case`.
