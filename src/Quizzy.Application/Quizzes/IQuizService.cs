namespace Quizzy.Application.Quizzes;

public interface IQuizService
{
    Task<IReadOnlyList<QuizDto>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<QuizDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

    Task<QuizDto> CreateAsync(CreateQuizInput input, CancellationToken cancellationToken = default);

    Task<QuizDto> UpdateAsync(Guid id, UpdateQuizInput input, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);
}

public record CreateQuizInput(string Title, string? Description, Guid? FolderId);

public record UpdateQuizInput(string Title, string? Description, Guid? FolderId);
