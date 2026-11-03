namespace Quizzy.Application.Attempts;

public record SubmitAnswerInput(Guid QuestionId, IReadOnlyList<Guid> SelectedOptionIds);

public record SubmitAttemptInput(IReadOnlyList<SubmitAnswerInput> Answers);

public record AttemptAnswerResultDto(
    Guid QuestionId,
    bool IsCorrect,
    IReadOnlyList<Guid> SelectedOptionIds,
    IReadOnlyList<Guid> CorrectOptionIds);

public record AttemptResultDto(
    Guid Id,
    Guid QuizId,
    int Score,
    int MaxScore,
    double Percentage,
    DateTime CompletedAt,
    IReadOnlyList<AttemptAnswerResultDto> Answers);

public record AttemptSummaryDto(
    Guid Id,
    Guid QuizId,
    int Score,
    int MaxScore,
    double Percentage,
    DateTime CompletedAt);
