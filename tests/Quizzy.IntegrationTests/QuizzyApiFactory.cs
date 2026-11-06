using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Testcontainers.PostgreSql;

namespace Quizzy.IntegrationTests;

/// <summary>
/// Hosts the API against a disposable PostgreSQL container for end-to-end tests.
/// </summary>
public class QuizzyApiFactory : WebApplicationFactory<Program>, IAsyncLifetime
{
    private const string TestSigningKey = "integration-tests-signing-key-at-least-32-bytes";

    private readonly PostgreSqlContainer _database = new PostgreSqlBuilder("postgres:17-alpine")
        .Build();

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        // Run outside Development so appsettings.Development.json and user-secrets do not load.
        builder.UseEnvironment("Testing");
    }

    public async Task InitializeAsync()
    {
        await _database.StartAsync();

        // Set before the host is built (on first CreateClient): the application reads the
        // connection string eagerly during start-up, and environment variables outrank
        // configuration files, so this is what the API actually binds to.
        Environment.SetEnvironmentVariable("ConnectionStrings__Postgres", _database.GetConnectionString());
        Environment.SetEnvironmentVariable("Jwt__SigningKey", TestSigningKey);
        Environment.SetEnvironmentVariable("Seed__AdminEmail", "admin@quizzy.local");
        Environment.SetEnvironmentVariable("Seed__AdminPassword", "Admin123!");
        Environment.SetEnvironmentVariable("Scheduler__Enabled", "false");
    }

    public new async Task DisposeAsync()
    {
        Environment.SetEnvironmentVariable("ConnectionStrings__Postgres", null);
        Environment.SetEnvironmentVariable("Jwt__SigningKey", null);
        Environment.SetEnvironmentVariable("Seed__AdminEmail", null);
        Environment.SetEnvironmentVariable("Seed__AdminPassword", null);
        Environment.SetEnvironmentVariable("Scheduler__Enabled", null);

        await _database.DisposeAsync();
        await base.DisposeAsync();
    }
}
