using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A user-defined grouping of decks.
/// </summary>
public class Folder : Entity
{
    public string Name { get; set; } = null!;

    public Guid OwnerId { get; set; }

    public User Owner { get; set; } = null!;

    public ICollection<Deck> Decks { get; set; } = new List<Deck>();
}
