using Quizzy.Domain.Entities;

namespace Quizzy.Application.Common.Interfaces;

/// <summary>
/// Issues signed access tokens for authenticated users.
/// </summary>
public interface IJwtTokenService
{
    string GenerateToken(User user);
}
