namespace Quizzy.Application.Auth;

public record RegisterInput(string Email, string DisplayName, string Password);

public record LoginInput(string Email, string Password);

public record AuthResult(string Token, Guid UserId, string Email, string DisplayName, string Role);
