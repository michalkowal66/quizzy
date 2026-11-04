using Quizzy.Application.Common;

namespace Quizzy.Application.Quizzes;

public interface IQuizService
{
    Task<PagedResult<QuizDto>> GetPagedAsync(PageQuery query, Guid? folderId, CancellationToken cancellationToken = default);

    Task<QuizDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

    Task<QuizDto> CreateAsync(CreateQuizInput input, CancellationToken cancellationToken = default);

    Task<QuizDto> UpdateAsync(Guid id, UpdateQuizInput input, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);
}

public record CreateQuizInput(string Title, string? Description, Guid? FolderId);

public record UpdateQuizInput(string Title, string? Description, Guid? FolderId);
