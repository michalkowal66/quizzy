using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
using Testcontainers.PostgreSql;

namespace Quizzy.IntegrationTests;

/// <summary>
/// Hosts the API against a disposable PostgreSQL container for end-to-end tests.
/// </summary>
public class QuizzyApiFactory : WebApplicationFactory<Program>, IAsyncLifetime
{
    private readonly PostgreSqlContainer _database = new PostgreSqlBuilder("postgres:17-alpine")
        .Build();

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.ConfigureAppConfiguration((_, config) =>
        {
            config.AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["ConnectionStrings:Postgres"] = _database.GetConnectionString(),
                ["Jwt:SigningKey"] = "integration-tests-signing-key-at-least-32-bytes",
                ["Seed:AdminEmail"] = "admin@quizzy.local",
                ["Seed:AdminPassword"] = "Admin123!",
            });
        });
    }

    public async Task InitializeAsync() => await _database.StartAsync();

    public new async Task DisposeAsync()
    {
        await _database.DisposeAsync();
        await base.DisposeAsync();
    }
}
