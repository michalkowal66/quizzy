namespace Quizzy.Application.Questions;

public interface IQuestionService
{
    Task<IReadOnlyList<QuestionDto>> GetByQuizAsync(Guid quizId, CancellationToken cancellationToken = default);

    Task<QuestionDto> CreateAsync(Guid quizId, QuestionInput input, CancellationToken cancellationToken = default);

    Task<QuestionDto> UpdateAsync(Guid quizId, Guid questionId, QuestionInput input, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid quizId, Guid questionId, CancellationToken cancellationToken = default);
}
