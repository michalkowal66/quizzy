namespace Quizzy.Application.Decks;

public record DeckDto(
    Guid Id,
    string Title,
    string? Description,
    Guid? FolderId,
    int FlashcardCount,
    DateTime CreatedAt,
    DateTime? UpdatedAt);
