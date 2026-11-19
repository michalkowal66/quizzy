using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;
using Quizzy.Domain.Enums;

namespace Quizzy.Application.Users;

public record UserSummaryDto(Guid Id, string Email, string DisplayName, string Role, DateTime CreatedAt);

public record UserDetailDto(
    Guid Id,
    string Email,
    string DisplayName,
    string Role,
    DateTime CreatedAt,
    int CurrentStreak,
    int LongestStreak,
    DateOnly? LastActivity,
    int DeckCount,
    int FlashcardCount,
    int QuizCount,
    int AttemptCount);

public interface IUserAdminService
{
    Task<IReadOnlyList<UserSummaryDto>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<UserDetailDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

    Task<UserSummaryDto> ChangeRoleAsync(Guid id, UserRole role, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);
}

public class UserAdminService : IUserAdminService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public UserAdminService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<IReadOnlyList<UserSummaryDto>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _db.Users
            .OrderBy(u => u.Email)
            .Select(u => new UserSummaryDto(
                u.Id,
                u.Email,
                u.DisplayName,
                u.Role.ToString(),
                u.CreatedAt))
            .ToListAsync(cancellationToken);
    }

    public async Task<UserDetailDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var user = await FindAsync(id, cancellationToken);

        var activityDates = await _db.StudyActivities
            .Where(a => a.UserId == id)
            .Select(a => a.Date)
            .ToListAsync(cancellationToken);

        var streak = StreakCalculator.Calculate(activityDates, DateOnly.FromDateTime(DateTime.UtcNow));

        var deckCount = await _db.Decks.CountAsync(d => d.OwnerId == id, cancellationToken);
        var flashcardCount = await _db.Flashcards.CountAsync(f => f.Deck.OwnerId == id, cancellationToken);
        var quizCount = await _db.Quizzes.CountAsync(q => q.OwnerId == id, cancellationToken);
        var attemptCount = await _db.QuizAttempts.CountAsync(a => a.UserId == id, cancellationToken);

        DateOnly? lastActivity = activityDates.Count == 0 ? null : activityDates.Max();

        return new UserDetailDto(
            user.Id,
            user.Email,
            user.DisplayName,
            user.Role.ToString(),
            user.CreatedAt,
            streak.Current,
            streak.Longest,
            lastActivity,
            deckCount,
            flashcardCount,
            quizCount,
            attemptCount);
    }

    public async Task<UserSummaryDto> ChangeRoleAsync(Guid id, UserRole role, CancellationToken cancellationToken = default)
    {
        // An administrator cannot change their own role. This also guarantees that at least one
        // administrator (the caller) always remains, so the role can never be stripped from every account.
        if (id == _currentUser.UserId)
        {
            throw new ConflictException("You cannot change your own role.");
        }

        var user = await FindAsync(id, cancellationToken);

        user.Role = role;
        await _db.SaveChangesAsync(cancellationToken);

        return ToSummary(user);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        // An administrator cannot delete their own account. As with role changes, this keeps the
        // caller in place and so prevents the last administrator from being removed.
        if (id == _currentUser.UserId)
        {
            throw new ConflictException("You cannot delete your own account.");
        }

        var user = await FindAsync(id, cancellationToken);

        // Owned folders, decks, quizzes, attempts and activity rows are removed by the database
        // through the cascade delete behaviour configured on their foreign keys.
        _db.Users.Remove(user);
        await _db.SaveChangesAsync(cancellationToken);
    }

    private async Task<User> FindAsync(Guid id, CancellationToken cancellationToken)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Id == id, cancellationToken);
        return user ?? throw new NotFoundException(nameof(User), id);
    }

    private static UserSummaryDto ToSummary(User user) =>
        new(user.Id, user.Email, user.DisplayName, user.Role.ToString(), user.CreatedAt);
}
