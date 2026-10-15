using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace Quizzy.Infrastructure.Persistence;

/// <summary>
/// Enables EF Core tooling (migrations) to create the context at design time
/// without running the API host.
/// </summary>
public class QuizzyDbContextFactory : IDesignTimeDbContextFactory<QuizzyDbContext>
{
    public QuizzyDbContext CreateDbContext(string[] args)
    {
        var connectionString =
            Environment.GetEnvironmentVariable("ConnectionStrings__Postgres")
            ?? "Host=localhost;Port=5432;Database=quizzy;Username=quizzy;Password=quizzy";

        var options = new DbContextOptionsBuilder<QuizzyDbContext>()
            .UseNpgsql(connectionString)
            .Options;

        return new QuizzyDbContext(options);
    }
}
