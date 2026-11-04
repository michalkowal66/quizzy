using System.Linq.Expressions;
using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Decks;

public class DeckService : IDeckService
{
    private static readonly Expression<Func<Deck, DeckDto>> Projection = d => new DeckDto(
        d.Id,
        d.Title,
        d.Description,
        d.FolderId,
        d.Flashcards.Count,
        d.CreatedAt,
        d.UpdatedAt);

    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public DeckService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<PagedResult<DeckDto>> GetPagedAsync(PageQuery query, Guid? folderId, CancellationToken cancellationToken = default)
    {
        var decks = _db.Decks.Where(d => d.OwnerId == _currentUser.UserId);

        if (folderId is not null)
        {
            decks = decks.Where(d => d.FolderId == folderId);
        }

        if (!string.IsNullOrWhiteSpace(query.Search))
        {
            var term = query.Search.Trim().ToLower();
            decks = decks.Where(d => d.Title.ToLower().Contains(term));
        }

        var totalCount = await decks.CountAsync(cancellationToken);

        var items = await decks
            .OrderByDescending(d => d.CreatedAt)
            .Skip((query.NormalizedPage - 1) * query.NormalizedPageSize)
            .Take(query.NormalizedPageSize)
            .Select(Projection)
            .ToListAsync(cancellationToken);

        return new PagedResult<DeckDto>(items, query.NormalizedPage, query.NormalizedPageSize, totalCount);
    }

    public async Task<DeckDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var deck = await _db.Decks
            .Where(d => d.Id == id && d.OwnerId == _currentUser.UserId)
            .Select(Projection)
            .FirstOrDefaultAsync(cancellationToken);

        return deck ?? throw new NotFoundException(nameof(Deck), id);
    }

    public async Task<DeckDto> CreateAsync(CreateDeckInput input, CancellationToken cancellationToken = default)
    {
        await EnsureFolderIsOwnedAsync(input.FolderId, cancellationToken);

        var deck = new Deck
        {
            Title = input.Title.Trim(),
            Description = Normalize(input.Description),
            FolderId = input.FolderId,
            OwnerId = _currentUser.UserId,
        };

        _db.Decks.Add(deck);
        await _db.SaveChangesAsync(cancellationToken);

        return new DeckDto(deck.Id, deck.Title, deck.Description, deck.FolderId, 0, deck.CreatedAt, deck.UpdatedAt);
    }

    public async Task<DeckDto> UpdateAsync(Guid id, UpdateDeckInput input, CancellationToken cancellationToken = default)
    {
        var deck = await FindOwnedAsync(id, cancellationToken);
        await EnsureFolderIsOwnedAsync(input.FolderId, cancellationToken);

        deck.Title = input.Title.Trim();
        deck.Description = Normalize(input.Description);
        deck.FolderId = input.FolderId;
        deck.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync(cancellationToken);

        return await GetByIdAsync(id, cancellationToken);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var deck = await FindOwnedAsync(id, cancellationToken);
        _db.Decks.Remove(deck);
        await _db.SaveChangesAsync(cancellationToken);
    }

    private async Task<Deck> FindOwnedAsync(Guid id, CancellationToken cancellationToken)
    {
        var deck = await _db.Decks.FirstOrDefaultAsync(
            d => d.Id == id && d.OwnerId == _currentUser.UserId,
            cancellationToken);

        return deck ?? throw new NotFoundException(nameof(Deck), id);
    }

    private async Task EnsureFolderIsOwnedAsync(Guid? folderId, CancellationToken cancellationToken)
    {
        if (folderId is null)
        {
            return;
        }

        var ownsFolder = await _db.Folders.AnyAsync(
            f => f.Id == folderId && f.OwnerId == _currentUser.UserId,
            cancellationToken);

        if (!ownsFolder)
        {
            throw new NotFoundException(nameof(Folder), folderId);
        }
    }

    private static string? Normalize(string? value) =>
        string.IsNullOrWhiteSpace(value) ? null : value.Trim();
}
