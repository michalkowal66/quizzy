using Quizzy.Application.Common.Interfaces;
using Quizzy.Infrastructure.Persistence;

namespace Quizzy.Api.Services;

/// <summary>
/// Resolves the account that owns resources for the current request.
/// </summary>
public class CurrentUserService : ICurrentUserService
{
    public Guid UserId => DbSeeder.DefaultUserId;
}
