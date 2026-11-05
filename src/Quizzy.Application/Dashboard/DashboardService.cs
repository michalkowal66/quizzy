using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common;
using Quizzy.Application.Common.Interfaces;

namespace Quizzy.Application.Dashboard;

public record DashboardDto(
    int CurrentStreak,
    int LongestStreak,
    bool StudiedToday,
    DateOnly? LastActivity,
    int DeckCount,
    int FlashcardCount,
    int QuizCount,
    int AttemptCount);

public interface IDashboardService
{
    Task<DashboardDto> GetAsync(CancellationToken cancellationToken = default);
}

public class DashboardService : IDashboardService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public DashboardService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<DashboardDto> GetAsync(CancellationToken cancellationToken = default)
    {
        var userId = _currentUser.UserId;
        var today = DateOnly.FromDateTime(DateTime.UtcNow);

        var activityDates = await _db.StudyActivities
            .Where(a => a.UserId == userId)
            .Select(a => a.Date)
            .ToListAsync(cancellationToken);

        var streak = StreakCalculator.Calculate(activityDates, today);

        var deckCount = await _db.Decks.CountAsync(d => d.OwnerId == userId, cancellationToken);
        var flashcardCount = await _db.Flashcards.CountAsync(f => f.Deck.OwnerId == userId, cancellationToken);
        var quizCount = await _db.Quizzes.CountAsync(q => q.OwnerId == userId, cancellationToken);
        var attemptCount = await _db.QuizAttempts.CountAsync(a => a.UserId == userId, cancellationToken);

        DateOnly? lastActivity = activityDates.Count == 0 ? null : activityDates.Max();

        return new DashboardDto(
            streak.Current,
            streak.Longest,
            activityDates.Contains(today),
            lastActivity,
            deckCount,
            flashcardCount,
            quizCount,
            attemptCount);
    }
}
