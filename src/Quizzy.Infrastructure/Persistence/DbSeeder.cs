using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;
using Quizzy.Domain.Enums;

namespace Quizzy.Infrastructure.Persistence;

/// <summary>
/// Ensures baseline data required for the application to run.
/// </summary>
public static class DbSeeder
{
    public static async Task SeedAsync(
        QuizzyDbContext db,
        IPasswordHasher passwordHasher,
        IConfiguration configuration,
        CancellationToken cancellationToken = default)
    {
        var email = configuration["Seed:AdminEmail"];
        var password = configuration["Seed:AdminPassword"];

        if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
        {
            return;
        }

        email = email.Trim().ToLowerInvariant();

        if (await db.Users.AnyAsync(u => u.Email == email, cancellationToken))
        {
            return;
        }

        db.Users.Add(new User
        {
            Email = email,
            DisplayName = "Administrator",
            PasswordHash = passwordHasher.Hash(password),
            Role = UserRole.Admin,
        });

        await db.SaveChangesAsync(cancellationToken);
    }
}
