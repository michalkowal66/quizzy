using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Interfaces;

namespace Quizzy.Application.Users;

public record UserSummaryDto(Guid Id, string Email, string DisplayName, string Role, DateTime CreatedAt);

public interface IUserDirectory
{
    Task<IReadOnlyList<UserSummaryDto>> GetAllAsync(CancellationToken cancellationToken = default);
}

public class UserDirectory : IUserDirectory
{
    private readonly IApplicationDbContext _db;

    public UserDirectory(IApplicationDbContext db) => _db = db;

    public async Task<IReadOnlyList<UserSummaryDto>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _db.Users
            .OrderBy(u => u.Email)
            .Select(u => new UserSummaryDto(
                u.Id,
                u.Email,
                u.DisplayName,
                u.Role.ToString(),
                u.CreatedAt))
            .ToListAsync(cancellationToken);
    }
}
