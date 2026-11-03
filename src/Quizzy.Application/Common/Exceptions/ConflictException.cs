namespace Quizzy.Application.Common.Exceptions;

/// <summary>
/// Thrown when an operation violates a uniqueness or state invariant.
/// </summary>
public class ConflictException : Exception
{
    public ConflictException(string message)
        : base(message)
    {
    }
}
