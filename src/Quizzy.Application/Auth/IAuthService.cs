namespace Quizzy.Application.Auth;

public interface IAuthService
{
    Task<AuthResult> RegisterAsync(RegisterInput input, CancellationToken cancellationToken = default);

    Task<AuthResult> LoginAsync(LoginInput input, CancellationToken cancellationToken = default);
}
