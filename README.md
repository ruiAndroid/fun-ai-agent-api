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

- `POST /api/v1/tasks`
- `GET /api/v1/tasks/{taskId}`
- `POST /api/v1/tasks/{taskId}/cancel`
- `GET /api/v1/tasks/{taskId}/events` (SSE)
