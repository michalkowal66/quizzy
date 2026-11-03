namespace Quizzy.Application.Quizzes;

public record QuizDto(
    Guid Id,
    string Title,
    string? Description,
    Guid? FolderId,
    int QuestionCount,
    DateTime CreatedAt,
    DateTime? UpdatedAt);
