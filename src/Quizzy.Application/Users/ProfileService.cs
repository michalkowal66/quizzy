using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Users;

public record ProfileDto(Guid Id, string Email, string DisplayName, string Role, DateTime CreatedAt);

public interface IProfileService
{
    Task<ProfileDto> GetAsync(CancellationToken cancellationToken = default);

    Task<ProfileDto> UpdateDisplayNameAsync(string displayName, CancellationToken cancellationToken = default);
}

public class ProfileService : IProfileService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public ProfileService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<ProfileDto> GetAsync(CancellationToken cancellationToken = default)
    {
        var user = await FindAsync(cancellationToken);
        return ToDto(user);
    }

    public async Task<ProfileDto> UpdateDisplayNameAsync(string displayName, CancellationToken cancellationToken = default)
    {
        var user = await FindAsync(cancellationToken);
        user.DisplayName = displayName.Trim();
        await _db.SaveChangesAsync(cancellationToken);
        return ToDto(user);
    }

    private async Task<User> FindAsync(CancellationToken cancellationToken)
    {
        var user = await _db.Users
            .FirstOrDefaultAsync(u => u.Id == _currentUser.UserId, cancellationToken);

        return user ?? throw new NotFoundException(nameof(User), _currentUser.UserId);
    }

    private static ProfileDto ToDto(User user) =>
        new(user.Id, user.Email, user.DisplayName, user.Role.ToString(), user.CreatedAt);
}
