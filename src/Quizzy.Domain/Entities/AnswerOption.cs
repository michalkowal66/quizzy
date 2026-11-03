using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// One selectable answer for a question.
/// </summary>
public class AnswerOption : Entity
{
    public Guid QuestionId { get; set; }

    public Question Question { get; set; } = null!;

    public string Text { get; set; } = null!;

    public bool IsCorrect { get; set; }
}
