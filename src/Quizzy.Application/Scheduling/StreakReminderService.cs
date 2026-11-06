using MassTransit;
using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Application.Messaging;

namespace Quizzy.Application.Scheduling;

public interface IStreakReminderService
{
    /// <summary>
    /// Publishes a reminder for every user who was active yesterday but not yet today,
    /// i.e. whose streak is still alive but will break unless they study. Returns the
    /// number of reminders dispatched.
    /// </summary>
    Task<int> DispatchRemindersAsync(CancellationToken cancellationToken = default);
}

public class StreakReminderService : IStreakReminderService
{
    private readonly IApplicationDbContext _db;
    private readonly IPublishEndpoint _publishEndpoint;

    public StreakReminderService(IApplicationDbContext db, IPublishEndpoint publishEndpoint)
    {
        _db = db;
        _publishEndpoint = publishEndpoint;
    }

    public async Task<int> DispatchRemindersAsync(CancellationToken cancellationToken = default)
    {
        var today = DateOnly.FromDateTime(DateTime.UtcNow);
        var yesterday = today.AddDays(-1);

        var candidates = await _db.Users
            .Where(u =>
                _db.StudyActivities.Any(a => a.UserId == u.Id && a.Date == yesterday) &&
                !_db.StudyActivities.Any(a => a.UserId == u.Id && a.Date == today))
            .Select(u => new { u.Id, u.Email, u.DisplayName })
            .ToListAsync(cancellationToken);

        foreach (var candidate in candidates)
        {
            var dates = await _db.StudyActivities
                .Where(a => a.UserId == candidate.Id)
                .Select(a => a.Date)
                .ToListAsync(cancellationToken);

            var streak = StreakCalculator.Calculate(dates, today).Current;

            await _publishEndpoint.Publish(
                new StreakReminderDue
                {
                    UserId = candidate.Id,
                    Email = candidate.Email,
                    DisplayName = candidate.DisplayName,
                    CurrentStreak = streak,
                },
                cancellationToken);
        }

        return candidates.Count;
    }
}
