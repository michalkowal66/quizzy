namespace Quizzy.Application.Common.Interfaces;

/// <summary>
/// Provides the identity of the user making the current request.
/// </summary>
public interface ICurrentUserService
{
    Guid UserId { get; }
}
