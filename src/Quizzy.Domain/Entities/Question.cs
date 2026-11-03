using Quizzy.Domain.Common;
using Quizzy.Domain.Enums;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A single question belonging to a quiz, together with its answer options.
/// </summary>
public class Question : Entity
{
    public Guid QuizId { get; set; }

    public Quiz Quiz { get; set; } = null!;

    public string Text { get; set; } = null!;

    public QuestionType Type { get; set; }

    public ICollection<AnswerOption> Options { get; set; } = new List<AnswerOption>();
}
