namespace Quizzy.Infrastructure.Security;

/// <summary>
/// Options controlling access-token generation and validation.
/// </summary>
public class JwtSettings
{
    public const string SectionName = "Jwt";

    public string Issuer { get; set; } = "quizzy";

    public string Audience { get; set; } = "quizzy";

    public string SigningKey { get; set; } = string.Empty;

    public int ExpiryMinutes { get; set; } = 60;
}
