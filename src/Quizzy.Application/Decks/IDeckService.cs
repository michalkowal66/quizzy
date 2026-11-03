namespace Quizzy.Application.Decks;

public interface IDeckService
{
    Task<IReadOnlyList<DeckDto>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<DeckDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

    Task<DeckDto> CreateAsync(CreateDeckInput input, CancellationToken cancellationToken = default);

    Task<DeckDto> UpdateAsync(Guid id, UpdateDeckInput input, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);
}

public record CreateDeckInput(string Title, string? Description, Guid? FolderId);

public record UpdateDeckInput(string Title, string? Description, Guid? FolderId);
