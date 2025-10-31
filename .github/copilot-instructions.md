# Copilot Instructions for PICSY_TrustLikePF

This repo is a 3-tier app orchestrated by Docker Compose. Use these notes to be productive quickly and align with project-specific patterns.

## Big picture

- Services: `backend-java` (Spring Boot 3.5 + Postgres + Flyway), `frontend-vue` (Vue 3 + Vite), `server-node` (Node/Express BFF that serves built SPA and proxies API).
- Data flow: Browser → BFF (`/api` proxy) → Spring Boot (`/api/**`) → Postgres. In local dev, Vite can proxy directly to the backend.
- DB schema/migrations live under `backend-java/src/main/resources/db/migration` (Flyway V1/V2). IDs are UUIDv4; JSONB is used for flexible fields.

## How to run (typical)

- Full stack via Docker: `docker-compose.yaml` builds UI → packages BFF → builds backend → starts Postgres.
  - Web (BFF+SPA): host `http://localhost:8080` → proxies `/api` to backend.
  - Backend: host `http://localhost:8081` (container 8080 mapped to 8081).
  - DB: host `localhost:5432` (db name `picsy_pf`, user `user`, pass `password`).
- Backend only (local): `backend-java/mvnw spring-boot:run` (uses `application-dev.properties`, expects Postgres on `localhost:5432`).
- Frontend dev: in `frontend-vue`, `npm install` then `npm run dev` (Vite on `http://localhost:5173`). `vite.config.js` proxies `/api` to backend.

## API and integration

- API base path is `/api`. Common groups:
  - Posts: `/api/posts` (e.g., `GET /feed`, `POST /{postId}/like`, `POST /{postId}/quote`)
  - Users: `/api/users` (e.g., `GET /{userId}/power`)
  - Admin: `/api/admin/**` (e.g., `GET /settings/recovery-gamma`, freeze/reactivate users)
  - Transactions: `/api/v1/tx` (`POST /like`, `POST /quote`)
- CORS is configured for `/api/**` (see `backend-java/src/main/java/com/picsy/trustlikepf/config/CorsConfig.java`).
- BFF proxies `/api` unchanged to `BACKEND_API_URL` (see `server-node/src/server.js`). Built SPA is served from `server-node/public`.

## Project conventions

- Backend
  - Java 21, Spring Boot 3.5, package root `com.picsy.trustlikepf` (`TrustlikepfApplication`).
  - Flyway drives schema; `spring.jpa.hibernate.ddl-auto=validate/none`. Prefer JSONB types (configured).
  - Healthcheck endpoints are functional routes; Compose checks `GET /api/posts/feed` for readiness.
  - Build output fixed to `target/app.jar` (see `<finalName>app</finalName>`); Dockerfile copies that exact name.
- Frontend
  - Central API client at `frontend-vue/src/lib/api.js` reading `import.meta.env.VITE_API_BASE` with default `/api`.
  - State via Pinia stores under `frontend-vue/src/stores/*`; components compose small, feature-focused views.
- BFF
  - Pure reverse proxy for `/api`; no path rewrites. Reads `process.env.BACKEND_API_URL` (Compose sets `http://backend:8080`).

## Commands you’ll need often

- Backend: `./mvnw -q -DskipTests package` → `target/app.jar`; run with `./mvnw spring-boot:run` in dev.
- Frontend: `npm run dev` (dev server), `npm run build` (dist to `frontend-vue/dist`).
- BFF: `node src/server.js` (serves `public` and proxies `/api`). In Docker, UI build artifacts are copied into `public`.

## Pitfalls and gotchas

- Vite proxy target should point to the backend’s 8080, e.g. `target: 'http://localhost:8080'` in `vite.config.js` (watch for typos like `808`).
- Compose uses a generous `start_period` for backend health to allow initial Flyway migrations; don’t remove unless you add Actuator health.
- Lombok is pinned and added to `maven-compiler-plugin.annotationProcessorPaths`. Keep that when adjusting Java/Maven to avoid annotation processing errors.

## Examples to follow

- New backend endpoint: add a `@RestController` under `com.picsy.trustlikepf.api` and mount under `/api/...`; expose DTOs in `api/dto` and rely on Flyway if schema changes.
- New frontend API call: use `src/lib/api.js` (base `/api`) and add a Pinia store action in `src/stores/...` to fetch; components should not hardcode base URLs.
- End-to-end check locally: run Postgres (via Compose or local), start backend on 8080, run Vite dev; navigate to `http://localhost:5173`.
