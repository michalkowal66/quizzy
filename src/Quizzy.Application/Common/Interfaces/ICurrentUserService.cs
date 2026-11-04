namespace Quizzy.Application.Common.Interfaces;

/// <summary>
/// Provides the identity of the user making the current request.
/// </summary>
public interface ICurrentUserService
{
    /// <summary>The authenticated user's identifier.</summary>
    Guid UserId { get; }

    bool IsAuthenticated { get; }
}
