using System.Linq.Expressions;
using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Quizzes;

public class QuizService : IQuizService
{
    private static readonly Expression<Func<Quiz, QuizDto>> Projection = q => new QuizDto(
        q.Id,
        q.Title,
        q.Description,
        q.FolderId,
        q.Questions.Count,
        q.CreatedAt,
        q.UpdatedAt);

    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public QuizService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<IReadOnlyList<QuizDto>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _db.Quizzes
            .Where(q => q.OwnerId == _currentUser.UserId)
            .OrderByDescending(q => q.CreatedAt)
            .Select(Projection)
            .ToListAsync(cancellationToken);
    }

    public async Task<QuizDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var quiz = await _db.Quizzes
            .Where(q => q.Id == id && q.OwnerId == _currentUser.UserId)
            .Select(Projection)
            .FirstOrDefaultAsync(cancellationToken);

        return quiz ?? throw new NotFoundException(nameof(Quiz), id);
    }

    public async Task<QuizDto> CreateAsync(CreateQuizInput input, CancellationToken cancellationToken = default)
    {
        await EnsureFolderIsOwnedAsync(input.FolderId, cancellationToken);

        var quiz = new Quiz
        {
            Title = input.Title.Trim(),
            Description = Normalize(input.Description),
            FolderId = input.FolderId,
            OwnerId = _currentUser.UserId,
        };

        _db.Quizzes.Add(quiz);
        await _db.SaveChangesAsync(cancellationToken);

        return new QuizDto(quiz.Id, quiz.Title, quiz.Description, quiz.FolderId, 0, quiz.CreatedAt, quiz.UpdatedAt);
    }

    public async Task<QuizDto> UpdateAsync(Guid id, UpdateQuizInput input, CancellationToken cancellationToken = default)
    {
        var quiz = await FindOwnedAsync(id, cancellationToken);
        await EnsureFolderIsOwnedAsync(input.FolderId, cancellationToken);

        quiz.Title = input.Title.Trim();
        quiz.Description = Normalize(input.Description);
        quiz.FolderId = input.FolderId;
        quiz.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync(cancellationToken);

        return await GetByIdAsync(id, cancellationToken);
    }

    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        var quiz = await FindOwnedAsync(id, cancellationToken);
        _db.Quizzes.Remove(quiz);
        await _db.SaveChangesAsync(cancellationToken);
    }

    private async Task<Quiz> FindOwnedAsync(Guid id, CancellationToken cancellationToken)
    {
        var quiz = await _db.Quizzes.FirstOrDefaultAsync(
            q => q.Id == id && q.OwnerId == _currentUser.UserId,
            cancellationToken);

        return quiz ?? throw new NotFoundException(nameof(Quiz), id);
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
