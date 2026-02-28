# fun-ai-agent-api

Control API service for managing lobster runtime instances.

## Tech Stack

- Java 17+
- Spring Boot 4.0.3
- Spring WebMVC + Validation + Actuator

## Run

```bash
mvn spring-boot:run
```

Default port: `8080`

## Current Scope

- `GET /v1/health`
- `GET /v1/instances`
- `POST /v1/instances`
- `POST /v1/instances/{instanceId}/actions`

Current persistence is in-memory for bootstrap development.
