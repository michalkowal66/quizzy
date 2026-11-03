namespace Quizzy.Application.Common.Exceptions;

/// <summary>
/// Thrown when a request is semantically invalid beyond simple field validation.
/// </summary>
public class BadRequestException : Exception
{
    public BadRequestException(string message)
        : base(message)
    {
    }
}
