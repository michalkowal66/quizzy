namespace Quizzy.Domain.Enums;

/// <summary>
/// Determines how a question is answered and scored.
/// </summary>
public enum QuestionType
{
    /// <summary>Exactly one option is correct.</summary>
    SingleChoice = 0,

    /// <summary>One or more options are correct and all must be selected.</summary>
    MultipleChoice = 1,

    /// <summary>A true/false question with exactly one correct option.</summary>
    TrueFalse = 2,
}
