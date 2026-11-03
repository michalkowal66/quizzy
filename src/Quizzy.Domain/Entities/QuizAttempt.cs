using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A completed take of a quiz by a user, with the resulting score.
/// </summary>
public class QuizAttempt : Entity
{
    public Guid QuizId { get; set; }

    public Quiz Quiz { get; set; } = null!;

    public Guid UserId { get; set; }

    public User User { get; set; } = null!;

    /// <summary>Number of questions answered correctly.</summary>
    public int Score { get; set; }

    /// <summary>Total number of questions in the quiz at the time of the attempt.</summary>
    public int MaxScore { get; set; }

    public ICollection<AttemptAnswer> Answers { get; set; } = new List<AttemptAnswer>();
}
