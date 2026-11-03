using Microsoft.EntityFrameworkCore;
using Quizzy.Domain.Entities;
using Quizzy.Domain.Enums;

namespace Quizzy.Infrastructure.Persistence;

/// <summary>
/// Ensures baseline data required for the application to run.
/// </summary>
public static class DbSeeder
{
    /// <summary>
    /// Identifier of the default account that owns resources until sign-in is wired up.
    /// </summary>
    public static readonly Guid DefaultUserId = new("11111111-1111-1111-1111-111111111111");

    public static async Task SeedAsync(QuizzyDbContext db, CancellationToken cancellationToken = default)
    {
        var defaultUserExists = await db.Users
            .AnyAsync(u => u.Id == DefaultUserId, cancellationToken);

        if (!defaultUserExists)
        {
            db.Users.Add(new User
            {
                Id = DefaultUserId,
                Email = "demo@quizzy.local",
                DisplayName = "Demo User",
                PasswordHash = string.Empty,
                Role = UserRole.User,
            });

            await db.SaveChangesAsync(cancellationToken);
        }
    }
}
