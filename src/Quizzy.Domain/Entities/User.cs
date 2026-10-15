using Quizzy.Domain.Common;
using Quizzy.Domain.Enums;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A registered account that owns decks, tests and study progress.
/// </summary>
public class User : Entity
{
    public string Email { get; set; } = null!;

    public string DisplayName { get; set; } = null!;

    public string PasswordHash { get; set; } = null!;

    public UserRole Role { get; set; } = UserRole.User;

    public ICollection<Folder> Folders { get; set; } = new List<Folder>();

    public ICollection<Deck> Decks { get; set; } = new List<Deck>();
}
