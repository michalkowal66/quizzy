namespace Quizzy.Application.Messaging;

/// <summary>
/// Published for a user whose active streak is at risk of breaking today,
/// so a reminder notification can be sent off the request thread.
/// </summary>
public record StreakReminderDue
{
    public required Guid UserId { get; init; }

    public required string Email { get; init; }

    public required string DisplayName { get; init; }

    public required int CurrentStreak { get; init; }
}
