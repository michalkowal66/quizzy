namespace Quizzy.Application.Messaging;

/// <summary>
/// Published after a quiz attempt is scored and stored, so that follow-up work
/// (activity tracking, notifications) happens off the request thread.
/// </summary>
public record QuizCompleted
{
    public required Guid AttemptId { get; init; }

    public required Guid QuizId { get; init; }

    public required Guid UserId { get; init; }

    public required int Score { get; init; }

    public required int MaxScore { get; init; }

    public required DateTime CompletedAt { get; init; }
}
