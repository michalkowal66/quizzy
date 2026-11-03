using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A user's response to a single question within a quiz attempt.
/// </summary>
public class AttemptAnswer : Entity
{
    public Guid QuizAttemptId { get; set; }

    public QuizAttempt QuizAttempt { get; set; } = null!;

    /// <summary>The question this answer refers to. Kept as a plain reference so that
    /// an attempt remains an immutable record even if the question is later changed.</summary>
    public Guid QuestionId { get; set; }

    /// <summary>Identifiers of the options the user selected for the question.</summary>
    public List<Guid> SelectedOptionIds { get; set; } = new();

    public bool IsCorrect { get; set; }
}
