namespace Quizzy.Application.Common.Exceptions;

/// <summary>
/// Thrown when the current user is not allowed to access a resource.
/// </summary>
public class ForbiddenAccessException : Exception
{
    public ForbiddenAccessException(string message = "Access to the requested resource is forbidden.")
        : base(message)
    {
    }
}
