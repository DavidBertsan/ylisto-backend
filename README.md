# YListo — Backend

[![CI](https://github.com/DavidBertsan/ylisto-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/DavidBertsan/ylisto-backend/actions/workflows/ci.yml)

REST API for **YListo**, a multi-tenant management tool for small businesses (butcher shops, bars, clothing stores): employee clock-in/out, team, work shifts, inventory with stock movements, profiles and 1-to-1 messaging, behind JWT authentication.

**Stack:** Java 25 · Spring Boot 4 · Spring Security (JWT) · Spring Data JPA · PostgreSQL · Testcontainers · JUnit 5 / Mockito · Docker · GitHub Actions

## Features

| Feature | Endpoints | Who |
|---|---|---|
| Business signup | `POST /signup`: creates the business and its admin in one transaction | Public |
| Login | `POST /login`: returns a signed JWT (8 h) | Public |
| Clock in / out | `POST /clockings/clock-in`, `POST /clockings/clock-out` | Any user |
| Team | `GET /team`, `POST /team`, `DELETE /team/{userId}` | Admin |
| Inventory | `GET/POST /products`, `GET /products/{productId}`, `GET/POST /products/{productId}/variants` | Read: any user · Write: admin |
| Shifts | `POST /shifts`, `DELETE /shifts/{shiftId}` · `GET /shifts/me?from=&to=` | Assign/remove: admin · My schedule: any user |
| Stock movements | `POST /movements` (sale, restock, adjustment) · `GET /movements` (log) | Register: any user · Log: admin |
| Profile | `GET /profile`, `PUT /profile`, `PUT /profile/password` | Any user (own profile only) |
| Messaging | `GET/POST /conversations`, `GET/POST /conversations/{conversationId}/messages?after=` | Participants only |

## Design highlights

- **Multi-tenant (shared schema).** Every business's data is isolated: the tenant comes from the JWT, never from the request, and every query filters by it (`findByIdAndBusinessId`). A resource from another business returns **404**, so its existence is never revealed.
- **Stateless JWT auth.** HS256-signed tokens carry the user id, business id and role. Roles map to Spring Security authorities (`hasRole("ADMIN")`).
- **Secure by default.** BCrypt passwords (changing it requires the current one), the role and the acting user are always taken from the token (never from the request body), one generic `401` for unknown user or wrong password (no user enumeration), and deactivated users can't log in or be messaged.
- **Package by feature** (`signup`, `auth`, `clocking`, `team`, `inventory`, `shift`, `user`, `messaging`). Repositories are package-private; features talk to each other through their services.
- **Domain rules in the right place.** Entities guard their own state (`applyMovement`, `updateProfile`); services orchestrate. One open clocking per user; every timestamp is set by the server (an injected `Clock`) and stored as `Instant` (UTC, DST-safe).
- **No overlapping shifts.** Two shifts overlap when `newStart < existing.end AND newEnd > existing.start`; back-to-back shifts are allowed.
- **Stock you can trust.** A movement and its stock change are saved in one transaction, stock can never go negative (`409`), and `@Version` optimistic locking stops two concurrent sales from silently overwriting each other.
- **One conversation per pair.** Participants are stored in a fixed order (smaller id first) behind a unique constraint, so Ana→Pepe and Pepe→Ana resolve to the same conversation and the database itself refuses a duplicate. Messages are delivered by polling (`?after=`); WebSocket can be added on top without changing the model.
- **Soft delete for employees.** Clock-in history is kept, since Spanish law requires keeping work-time records.
- **Errors as RFC 9457 `ProblemDetail`**, with consistent 400 / 401 / 403 / 404 / 409 responses.

## Run locally

Requirements: JDK 25 and Docker.

```bash
docker compose up -d
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

## Try it

```bash
# 1. Sign up a business (you become its ADMIN)
curl -X POST localhost:8080/signup -H "Content-Type: application/json" -d '{
  "businessName": "Carniceria Pepe", "businessType": "Butcher",
  "username": "pepe", "password": "a-long-password", "name": "Pepe", "mobile": "600000000"
}'

# 2. Log in and keep the token
TOKEN=$(curl -s -X POST localhost:8080/login -H "Content-Type: application/json" \
  -d '{"username": "pepe", "password": "a-long-password"}' | jq -r .token)

# 3. Clock in (the user comes from the token)
curl -X POST localhost:8080/clockings/clock-in -H "Authorization: Bearer $TOKEN"

# 4. Add an employee to your team
curl -X POST localhost:8080/team -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"username": "lucia", "password": "another-long-password", "name": "Lucia", "mobile": "611111111"}'

# 5. Create a product and a variant with its stock
PRODUCT_ID=$(curl -s -X POST localhost:8080/products -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"name": "Pollo", "unit": "KILOGRAM"}' | jq -r .id)

VARIANT_ID=$(curl -s -X POST localhost:8080/products/$PRODUCT_ID/variants -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"label": "Pechuga", "price": 7.99, "quantity": 12.5}' | jq -r .id)

# 6. Sell 2.5 kg (stock goes from 12.5 to 10)
curl -X POST localhost:8080/movements -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d "{\"variantId\": \"$VARIANT_ID\", \"type\": \"SALE\", \"quantity\": 2.5}"
```

## Tests

```bash
./mvnw verify
```

105 tests on three levels:

- **Unit** (JUnit 5 + Mockito, frozen `Clock`): business rules in services.
- **Web slice** (`@WebMvcTest` with the real security config): HTTP contract, validation, status codes and role checks.
- **Integration** (`@SpringBootTest` / `@DataJpaTest` against a real PostgreSQL via Testcontainers): tenant isolation, transaction rollback, dirty checking, optimistic locking, database unique constraints, and the full signup → login → clock-in flow over HTTP.

Docker must be running for the integration tests.

## Roadmap

- Flyway migrations and cloud deployment
- Mobile app with Compose Multiplatform (Android first)
- Real-time messaging over WebSocket, group chats and read receipts
