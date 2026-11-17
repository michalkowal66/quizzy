# Quizzy

A web platform that helps students learn by creating their own study material:
flashcard decks and tests (single-choice, multiple-choice, true/false). The app saves
study progress and results, and motivates regular learning by tracking a daily-activity
streak on the user dashboard.

## Features

- **Accounts** — registration, login, and JWT-based sessions, with `User` and `Admin` roles.
- **Decks & flashcards** — create, edit, search and paginate decks; add flashcards and review
  them in a flip-card study mode.
- **Quizzes** — build quizzes with single-choice, multiple-choice and true/false questions.
- **Solving & scoring** — take a quiz and get a server-computed score with per-question
  feedback; every attempt is stored.
- **Dashboard & streak** — a daily learning streak, the longest streak, and counts of decks,
  flashcards, quizzes and attempts.
- **Resource ownership** — users only see and modify their own material; admins can list users.
- **Async processing** — completing a quiz publishes an event that a background consumer
  handles off the request thread (recording study activity), plus a daily scheduler for
  streak reminders.

## Tech stack

| Layer       | Technology                                                                                           |
| ----------- | ---------------------------------------------------------------------------------------------------- |
| Backend     | ASP.NET Core (.NET 10) + FastEndpoints                                                               |
| Data access | EF Core + Npgsql (PostgreSQL) with migrations                                                        |
| Database    | PostgreSQL                                                                                           |
| Messaging   | RabbitMQ + MassTransit (async event processing)                                                      |
| Frontend    | React + TypeScript (Vite), Tailwind + shadcn/ui, TanStack Query, React Router, React Hook Form + Zod |
| Tests       | xUnit (unit) + WebApplicationFactory & Testcontainers (integration)                                  |
| CI          | GitHub Actions                                                                                       |

## Solution structure

```
src/
  Quizzy.Api             ASP.NET Core host, HTTP endpoints, DTOs, validation, error handling
  Quizzy.Application     business logic, services, use cases, event contracts
  Quizzy.Domain          entities, enums, domain rules
  Quizzy.Infrastructure  EF Core DbContext, migrations, external integrations
frontend/                React + TypeScript single-page application
tests/                   unit and integration test projects
docs/                    data model, API specification
```

## Run the whole stack with Docker

The fastest way to run everything — database, message broker, API and web client:

```bash
cp .env.example .env            # then set Jwt__SigningKey to a long random secret
docker compose up --build
```

- Web client: <http://localhost:8080>
- API + Swagger UI: <http://localhost:8081/swagger>
- RabbitMQ management UI: <http://localhost:15672>

The API applies database migrations automatically on startup, so the stack is ready to use
once the containers report healthy. Published ports can be changed with `WEB_PORT` and
`API_PORT` in `.env`.

## Local development

Prerequisites: .NET 10 SDK, Docker, Node.js.

```bash
# 1. Start the database. RabbitMQ is optional — see the note below.
cp .env.example .env
docker compose up -d postgres

# 2. Provide the local JWT signing key
dotnet user-secrets set "Jwt:SigningKey" "<a-long-random-secret>" --project src/Quizzy.Api

# 3. Run the API (http://localhost:5289); use `dotnet watch` for hot reload
dotnet run --project src/Quizzy.Api

# 4. Run the web client (http://localhost:5173)
cd frontend
npm install
npm run dev
```

When no broker is configured (`RabbitMq:Host` unset), the message bus runs
in-process, so the API and the test suite work without RabbitMQ. Start it with
`docker compose up -d rabbitmq` only when exercising the asynchronous flow.

In the development environment the database is populated on first run with a
demo account and sample decks, quizzes and activity, so there is something to
explore right away. Sign in as `demo@quizzy.local` with password `demo12345`.
This is controlled by `Seed:SampleData` (enabled in
`appsettings.Development.json`) and never runs outside Development.

## Configuration

Configuration is layered: `appsettings.json` holds non-sensitive defaults, environment
variables (and `.env` for Docker) override them, and local secrets live in .NET
user-secrets. Nested keys use `__` as the separator in environment variables.

| Key                                       | Required | Description                                                 |
| ----------------------------------------- | -------- | ----------------------------------------------------------- |
| `ConnectionStrings:Postgres`              | yes      | PostgreSQL connection string                                |
| `Jwt:SigningKey`                          | yes      | Secret used to sign access tokens (min. 32 bytes)           |
| `Jwt:Issuer` / `Jwt:Audience`             | yes      | Token issuer and audience (defaulted in `appsettings.json`) |
| `Jwt:ExpiryMinutes`                       | no       | Access-token lifetime (default 60)                          |
| `Seed:AdminEmail` / `Seed:AdminPassword`  | no       | When both are set, an admin account is created on first run |
| `RabbitMq:Host`                           | no       | Message broker host; when unset the bus runs in-process     |
| `RabbitMq:Username` / `RabbitMq:Password` | no       | Broker credentials (default `guest`)                        |

Missing or invalid configuration fails fast at startup.

## Data model

Nine related entities back the application. See [docs/erd.md](docs/erd.md) for the full
entity-relationship diagram and notes.

## REST API

The API is documented with OpenAPI. Browse it interactively at `/swagger` while the API is
running, or read the exported specification in [docs/openapi.json](docs/openapi.json).

All endpoints except `/health`, `/auth/register` and `/auth/login` require a
`Bearer <token>` access token.

| Area       | Endpoints                                                                             |
| ---------- | ------------------------------------------------------------------------------------- |
| Auth       | `POST /auth/register`, `POST /auth/login`                                             |
| Profile    | `GET /me`, `PUT /me`                                                                  |
| Folders    | `POST /folders`, `GET /folders`, `GET/PUT/DELETE /folders/{id}`                       |
| Decks      | `POST /decks`, `GET /decks`, `GET/PUT/DELETE /decks/{id}`                             |
| Flashcards | `POST/GET /decks/{deckId}/flashcards`, `PUT/DELETE /decks/{deckId}/flashcards/{id}`   |
| Quizzes    | `POST /quizzes`, `GET /quizzes`, `GET/PUT/DELETE /quizzes/{id}`                       |
| Questions  | `POST/GET /quizzes/{quizId}/questions`, `PUT/DELETE /quizzes/{quizId}/questions/{id}` |
| Attempts   | `POST /quizzes/{quizId}/attempts`, `GET /quizzes/{quizId}/attempts`                   |
| Study      | `POST /study/log`                                                                     |
| Dashboard  | `GET /dashboard`                                                                      |
| Admin      | `GET /admin/users` (admin only)                                                       |
| Health     | `GET /health`                                                                         |

`GET /decks` and `GET /quizzes` support pagination (`page`, `pageSize`) and a `search` filter.

## Testing

```bash
dotnet test                     # unit + integration tests
```

Integration tests spin up a disposable PostgreSQL container with Testcontainers, so Docker
must be running. The frontend is checked with `npm run build` and `npm run lint` from the
`frontend` directory.
