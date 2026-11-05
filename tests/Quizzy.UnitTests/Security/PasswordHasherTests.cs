using Quizzy.Infrastructure.Security;

namespace Quizzy.UnitTests.Security;

public class PasswordHasherTests
{
    private readonly PasswordHasher _hasher = new();

    [Fact]
    public void Hash_ProducesVerifiableHash()
    {
        var hash = _hasher.Hash("Secret123!");

        Assert.True(_hasher.Verify("Secret123!", hash));
    }

    [Fact]
    public void Hash_ProducesDifferentHashesForSamePassword()
    {
        var first = _hasher.Hash("Secret123!");
        var second = _hasher.Hash("Secret123!");

        Assert.NotEqual(first, second);
    }

    [Fact]
    public void Verify_WrongPassword_ReturnsFalse()
    {
        var hash = _hasher.Hash("Secret123!");

        Assert.False(_hasher.Verify("WrongPassword", hash));
    }

    [Fact]
    public void Verify_EmptyHash_ReturnsFalse()
    {
        Assert.False(_hasher.Verify("anything", string.Empty));
    }
}
