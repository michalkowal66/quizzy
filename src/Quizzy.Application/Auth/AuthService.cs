using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;
using Quizzy.Domain.Enums;

namespace Quizzy.Application.Auth;

public class AuthService : IAuthService
{
    private readonly IApplicationDbContext _db;
    private readonly IPasswordHasher _passwordHasher;
    private readonly IJwtTokenService _tokenService;

    public AuthService(
        IApplicationDbContext db,
        IPasswordHasher passwordHasher,
        IJwtTokenService tokenService)
    {
        _db = db;
        _passwordHasher = passwordHasher;
        _tokenService = tokenService;
    }

    public async Task<AuthResult> RegisterAsync(RegisterInput input, CancellationToken cancellationToken = default)
    {
        var email = input.Email.Trim().ToLowerInvariant();

        var emailTaken = await _db.Users.AnyAsync(u => u.Email == email, cancellationToken);
        if (emailTaken)
        {
            throw new ConflictException($"An account with email '{email}' already exists.");
        }

        var user = new User
        {
            Email = email,
            DisplayName = input.DisplayName.Trim(),
            PasswordHash = _passwordHasher.Hash(input.Password),
            Role = UserRole.User,
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync(cancellationToken);

        return CreateResult(user);
    }

    public async Task<AuthResult> LoginAsync(LoginInput input, CancellationToken cancellationToken = default)
    {
        var email = input.Email.Trim().ToLowerInvariant();

        var user = await _db.Users.FirstOrDefaultAsync(u => u.Email == email, cancellationToken);

        if (user is null || !_passwordHasher.Verify(input.Password, user.PasswordHash))
        {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return CreateResult(user);
    }

    private AuthResult CreateResult(User user) => new(
        _tokenService.GenerateToken(user),
        user.Id,
        user.Email,
        user.DisplayName,
        user.Role.ToString());
}
