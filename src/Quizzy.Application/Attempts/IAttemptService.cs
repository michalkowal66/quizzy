namespace Quizzy.Application.Attempts;

public interface IAttemptService
{
    Task<AttemptResultDto> SubmitAsync(Guid quizId, SubmitAttemptInput input, CancellationToken cancellationToken = default);

    Task<IReadOnlyList<AttemptSummaryDto>> GetByQuizAsync(Guid quizId, CancellationToken cancellationToken = default);
}
