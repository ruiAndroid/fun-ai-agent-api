# fun-ai-agent-api

Control API service for managing claw instances.

## Tech Stack

- Java 17+
- Spring Boot 4.0.3
- Spring WebMVC + Validation + Actuator + JDBC
- PostgreSQL

## Run

```bash
mvn spring-boot:run
```

Default port: `8080`

Default database config is in `src/main/resources/application.yml` and points to:

- `jdbc:postgresql://172.21.138.98:5432/fun_ai_agent`
- username: `funai_agent`

Schema is auto-initialized at startup via `src/main/resources/schema.sql`.

## Current Scope

- `GET /v1/health`
- `GET /v1/instances`
- `POST /v1/instances`
- `POST /v1/instances/{instanceId}/actions`

## Update Script

Use `update-agent-api.sh` for one-command update on server:

```bash
chmod +x /opt/fun-ai-agent-api/update-agent-api.sh
/opt/fun-ai-agent-api/update-agent-api.sh
```

Optional environment variables:

- `APP_DIR` (default: `/opt/fun-ai-agent-api`)
- `SERVICE_NAME` (default: `fun-ai-agent-api`)
- `GIT_REMOTE` (default: `origin`)
- `GIT_BRANCH` (default: `main`)
- `HEALTH_URL` (default: `http://127.0.0.1:8080/v1/health`)
- `MVN_CMD` (default: `mvn`, Maven >= `3.6.3`)
