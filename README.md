# Quizzy

A web platform that helps students learn by creating their own study material:
flashcard decks and tests (single-choice, multiple-choice, true/false). The app saves
study progress and results, and motivates regular learning by tracking a daily-activity
streak on the user dashboard.

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
```

## Getting started

Prerequisites: .NET 10 SDK, Docker, Node.js.

```bash
# 1. Configure environment
cp .env.example .env            # then edit values

# 2. Start infrastructure (PostgreSQL)
docker compose up -d postgres

# 3. Run the API
dotnet run --project src/Quizzy.Api
# API available at http://localhost:5xxx — health check at /health
```

Full `docker compose up` for the whole stack (backend, frontend, database, broker) is
added in a later stage.

## Documentation

- Data model diagram (ERD) — _added later_
- REST API documentation (Swagger / OpenAPI) — _added later_
