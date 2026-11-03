using Quizzy.Domain.Enums;

namespace Quizzy.Application.Questions;

public record AnswerOptionDto(Guid Id, string Text, bool IsCorrect);

public record QuestionDto(
    Guid Id,
    Guid QuizId,
    string Text,
    QuestionType Type,
    IReadOnlyList<AnswerOptionDto> Options);

public record OptionInput(string Text, bool IsCorrect);

public record QuestionInput(string Text, QuestionType Type, IReadOnlyList<OptionInput> Options);
