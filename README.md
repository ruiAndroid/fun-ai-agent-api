# fun-ai-agent-api

Unified API gateway for the agent platform.

## Tech stack

- Java 17
- Spring Boot 3 (WebFlux)
- PostgreSQL
- Flyway

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
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_MIGRATION_ENABLED`
- `GATEWAY_BASE_URL`
- `GATEWAY_TOKEN`
- `GATEWAY_TIMEOUT_SECONDS`
- `GATEWAY_MODELS_CACHE_SECONDS`

Or use file-based config (no env vars):

- copy `application-private.example.yml` to `application-private.yml`
- set `gateway.token` in `application-private.yml`
- app auto-loads this file via `spring.config.import`
- make sure runtime env does not set `GATEWAY_TOKEN` to empty string

## Endpoints

- `POST /api/v1/tasks` (compatible: `/v1/tasks`)
- `GET /api/v1/tasks/{taskId}` (compatible: `/v1/tasks/{taskId}`)
- `POST /api/v1/tasks/{taskId}/cancel` (compatible: `/v1/tasks/{taskId}/cancel`)
- `GET /api/v1/tasks/{taskId}/events` (SSE, compatible: `/v1/tasks/{taskId}/events`)
- `GET /api/v1/config/agents` (compatible: `/v1/config/agents`)
- `PUT /api/v1/config/agents` (compatible: `/v1/config/agents`)
- `GET /api/v1/models` (compatible: `/v1/models`)

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

## Runtime config persistence

- Flyway migration creates tables under schema `agent_cfg`.
- `PUT /v1/config/agents` accepts full agent/workflow/skill config and replaces persisted config atomically.
- `GET /v1/config/agents` returns normalized config in frontend-friendly `camelCase`.
