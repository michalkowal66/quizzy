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
# 1. Start infrastructure
cp .env.example .env            # then edit values
docker compose up -d postgres

# 2. Provide the local JWT signing key
dotnet user-secrets set "Jwt:SigningKey" "<a-long-random-secret>" --project src/Quizzy.Api

# 3. Run the API
dotnet run --project src/Quizzy.Api
```

- Health check: `GET /health`
- API documentation (Swagger UI): `/swagger`

## Configuration

Configuration is layered: `appsettings.json` holds non-sensitive defaults, environment
variables (and `.env` for Docker) override them, and local secrets live in .NET
user-secrets. Nested keys use `__` as the separator in environment variables.

| Key                                      | Required | Description                                                 |
| ---------------------------------------- | -------- | ----------------------------------------------------------- |
| `ConnectionStrings:Postgres`             | yes      | PostgreSQL connection string                                |
| `Jwt:SigningKey`                         | yes      | Secret used to sign access tokens (min. 32 bytes)           |
| `Jwt:Issuer` / `Jwt:Audience`            | yes      | Token issuer and audience (defaulted in `appsettings.json`) |
| `Jwt:ExpiryMinutes`                      | no       | Access-token lifetime (default 60)                          |
| `Seed:AdminEmail` / `Seed:AdminPassword` | no       | When both are set, an admin account is created on first run |

Missing or invalid configuration fails fast at startup.
