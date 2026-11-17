# YListo — Backend

REST API for YListo, a management tool for small businesses (butcher shops, bars, clothing stores): clock-in, schedules, team, adaptable inventory and an activity log.

**Stack:** Java 25 · Spring Boot 4 · Spring Data JPA · PostgreSQL · Testcontainers · GitHub Actions

## Run locally

Requirements: JDK 25 and Docker.

```bash
docker compose up -d
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

## Tests

```bash
./mvnw verify
```

Integration tests start their own PostgreSQL with Testcontainers, so Docker must be running.
