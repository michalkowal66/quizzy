namespace Quizzy.Application.Common.Exceptions;

/// <summary>
/// Thrown when authentication fails, for example on invalid credentials.
/// </summary>
public class UnauthorizedException : Exception
{
    public UnauthorizedException(string message = "Authentication failed.")
        : base(message)
    {
    }
}
