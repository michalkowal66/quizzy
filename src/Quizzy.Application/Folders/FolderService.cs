using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Folders;

public class FolderService : IFolderService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public FolderService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<IReadOnlyList<FolderDto>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _db.Folders
            .Where(f => f.OwnerId == _currentUser.UserId)
            .OrderBy(f => f.Name)
            .Select(f => new FolderDto(
                f.Id,
                f.Name,
                f.Decks.Count,
                _db.Quizzes.Count(q => q.FolderId == f.Id),
                f.CreatedAt))
            .ToListAsync(cancellationToken);
    }

    public async Task<FolderDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var folder = await FindOwnedAsync(id, cancellationToken);
        var quizCount = await _db.Quizzes.CountAsync(q => q.FolderId == folder.Id, cancellationToken);
        return new FolderDto(folder.Id, folder.Name, folder.Decks.Count, quizCount, folder.CreatedAt);
    }

    public async Task<FolderDto> CreateAsync(string name, CancellationToken cancellationToken = default)
    {
        await EnsureNameIsUniqueAsync(name, excludedId: null, cancellationToken);

        var folder = new Folder
        {
            Name = name.Trim(),
            OwnerId = _currentUser.UserId,
        };

        _db.Folders.Add(folder);
        await SaveTranslatingNameConflictAsync(folder.Name, excludedId: null, cancellationToken);

        return new FolderDto(folder.Id, folder.Name, 0, 0, folder.CreatedAt);
    }

    public async Task<FolderDto> RenameAsync(Guid id, string name, CancellationToken cancellationToken = default)
    {
        var folder = await FindOwnedAsync(id, cancellationToken);
        await EnsureNameIsUniqueAsync(name, excludedId: id, cancellationToken);

        folder.Name = name.Trim();
        await SaveTranslatingNameConflictAsync(folder.Name, excludedId: id, cancellationToken);

        var quizCount = await _db.Quizzes.CountAsync(q => q.FolderId == folder.Id, cancellationToken);
        return new FolderDto(folder.Id, folder.Name, folder.Decks.Count, quizCount, folder.CreatedAt);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var folder = await FindOwnedAsync(id, cancellationToken);
        _db.Folders.Remove(folder);
        await _db.SaveChangesAsync(cancellationToken);
    }

    private async Task SaveTranslatingNameConflictAsync(string name, Guid? excludedId, CancellationToken cancellationToken)
    {
        try
        {
            await _db.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateException)
        {
            await EnsureNameIsUniqueAsync(name, excludedId, cancellationToken);
            throw;
        }
    }

    private async Task<Folder> FindOwnedAsync(Guid id, CancellationToken cancellationToken)
    {
        var folder = await _db.Folders
            .Include(f => f.Decks)
            .FirstOrDefaultAsync(
                f => f.Id == id && f.OwnerId == _currentUser.UserId,
                cancellationToken);

        return folder ?? throw new NotFoundException(nameof(Folder), id);
    }

    private async Task EnsureNameIsUniqueAsync(string name, Guid? excludedId, CancellationToken cancellationToken)
    {
        var normalized = name.Trim();

        var exists = await _db.Folders.AnyAsync(
            f => f.OwnerId == _currentUser.UserId
                && f.Id != excludedId
                && f.Name.ToLower() == normalized.ToLower(),
            cancellationToken);

        if (exists)
        {
            throw new ConflictException($"A folder named '{normalized}' already exists.");
        }
    }
}
