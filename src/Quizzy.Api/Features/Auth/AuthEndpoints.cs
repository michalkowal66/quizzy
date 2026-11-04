using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Auth;

namespace Quizzy.Api.Features.Auth;

public class RegisterRequest
{
    public string Email { get; set; } = null!;

    public string DisplayName { get; set; } = null!;

    public string Password { get; set; } = null!;
}

public class RegisterValidator : Validator<RegisterRequest>
{
    public RegisterValidator()
    {
        RuleFor(x => x.Email)
            .NotEmpty()
            .EmailAddress()
            .MaximumLength(256);

        RuleFor(x => x.DisplayName)
            .NotEmpty()
            .MaximumLength(100);

        RuleFor(x => x.Password)
            .NotEmpty()
            .MinimumLength(8)
            .MaximumLength(128);
    }
}

public class RegisterEndpoint : Endpoint<RegisterRequest, AuthResult>
{
    private readonly IAuthService _auth;

    public RegisterEndpoint(IAuthService auth) => _auth = auth;

    public override void Configure()
    {
        Post("/auth/register");
        AllowAnonymous();
    }

    public override async Task HandleAsync(RegisterRequest req, CancellationToken ct)
    {
        var result = await _auth.RegisterAsync(
            new RegisterInput(req.Email, req.DisplayName, req.Password), ct);
        await Send.ResponseAsync(result, StatusCodes.Status201Created, ct);
    }
}

public class LoginRequest
{
    public string Email { get; set; } = null!;

    public string Password { get; set; } = null!;
}

public class LoginValidator : Validator<LoginRequest>
{
    public LoginValidator()
    {
        RuleFor(x => x.Email).NotEmpty();
        RuleFor(x => x.Password).NotEmpty();
    }
}

public class LoginEndpoint : Endpoint<LoginRequest, AuthResult>
{
    private readonly IAuthService _auth;

    public LoginEndpoint(IAuthService auth) => _auth = auth;

    public override void Configure()
    {
        Post("/auth/login");
        AllowAnonymous();
    }

    public override async Task HandleAsync(LoginRequest req, CancellationToken ct)
    {
        var result = await _auth.LoginAsync(new LoginInput(req.Email, req.Password), ct);
        await Send.OkAsync(result, ct);
    }
}
