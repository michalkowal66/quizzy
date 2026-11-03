using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Flashcards;

public class FlashcardService : IFlashcardService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public FlashcardService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<IReadOnlyList<FlashcardDto>> GetByDeckAsync(Guid deckId, CancellationToken cancellationToken = default)
    {
        await EnsureDeckIsOwnedAsync(deckId, cancellationToken);

        return await _db.Flashcards
            .Where(f => f.DeckId == deckId)
            .OrderBy(f => f.CreatedAt)
            .Select(f => new FlashcardDto(f.Id, f.DeckId, f.Front, f.Back, f.CreatedAt))
            .ToListAsync(cancellationToken);
    }

    public async Task<FlashcardDto> CreateAsync(Guid deckId, string front, string back, CancellationToken cancellationToken = default)
    {
        await EnsureDeckIsOwnedAsync(deckId, cancellationToken);

        var flashcard = new Flashcard
        {
            DeckId = deckId,
            Front = front.Trim(),
            Back = back.Trim(),
        };

        _db.Flashcards.Add(flashcard);
        await _db.SaveChangesAsync(cancellationToken);

        return ToDto(flashcard);
    }

    public async Task<FlashcardDto> UpdateAsync(Guid deckId, Guid id, string front, string back, CancellationToken cancellationToken = default)
    {
        var flashcard = await FindOwnedAsync(deckId, id, cancellationToken);

        flashcard.Front = front.Trim();
        flashcard.Back = back.Trim();
        await _db.SaveChangesAsync(cancellationToken);

        return ToDto(flashcard);
    }

    public async Task DeleteAsync(Guid deckId, Guid id, CancellationToken cancellationToken = default)
    {
        var flashcard = await FindOwnedAsync(deckId, id, cancellationToken);
        _db.Flashcards.Remove(flashcard);
        await _db.SaveChangesAsync(cancellationToken);
    }

    private async Task<Flashcard> FindOwnedAsync(Guid deckId, Guid id, CancellationToken cancellationToken)
    {
        var flashcard = await _db.Flashcards.FirstOrDefaultAsync(
            f => f.Id == id
                && f.DeckId == deckId
                && f.Deck.OwnerId == _currentUser.UserId,
            cancellationToken);

        return flashcard ?? throw new NotFoundException(nameof(Flashcard), id);
    }

    private async Task EnsureDeckIsOwnedAsync(Guid deckId, CancellationToken cancellationToken)
    {
        var ownsDeck = await _db.Decks.AnyAsync(
            d => d.Id == deckId && d.OwnerId == _currentUser.UserId,
            cancellationToken);

        if (!ownsDeck)
        {
            throw new NotFoundException(nameof(Deck), deckId);
        }
    }

    private static FlashcardDto ToDto(Flashcard f) =>
        new(f.Id, f.DeckId, f.Front, f.Back, f.CreatedAt);
}
