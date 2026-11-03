namespace Quizzy.Application.Flashcards;

public interface IFlashcardService
{
    Task<IReadOnlyList<FlashcardDto>> GetByDeckAsync(Guid deckId, CancellationToken cancellationToken = default);

    Task<FlashcardDto> CreateAsync(Guid deckId, string front, string back, CancellationToken cancellationToken = default);

    Task<FlashcardDto> UpdateAsync(Guid deckId, Guid id, string front, string back, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid deckId, Guid id, CancellationToken cancellationToken = default);
}
