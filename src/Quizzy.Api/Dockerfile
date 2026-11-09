# Build the API together with the projects it references.
# Build context is the repository root (see docker-compose.yml).
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# Restore using only the project files first so the package layer is cached
# and reused whenever only source code changes.
COPY Directory.Build.props ./
COPY src/Quizzy.Domain/Quizzy.Domain.csproj src/Quizzy.Domain/
COPY src/Quizzy.Application/Quizzy.Application.csproj src/Quizzy.Application/
COPY src/Quizzy.Infrastructure/Quizzy.Infrastructure.csproj src/Quizzy.Infrastructure/
COPY src/Quizzy.Api/Quizzy.Api.csproj src/Quizzy.Api/
RUN dotnet restore src/Quizzy.Api/Quizzy.Api.csproj

# Copy the rest of the sources and publish a release build.
COPY src/ src/
RUN dotnet publish src/Quizzy.Api/Quizzy.Api.csproj -c Release -o /app --no-restore

# Run the published output on the smaller ASP.NET runtime image.
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS final
WORKDIR /app

# curl backs the container health check below.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app ./

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=10 \
    CMD curl -fsS http://localhost:8080/health || exit 1

ENTRYPOINT ["dotnet", "Quizzy.Api.dll"]
