namespace Quizzy.Application.Flashcards;

public record FlashcardDto(Guid Id, Guid DeckId, string Front, string Back, DateTime CreatedAt);
