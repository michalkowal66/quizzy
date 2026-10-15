using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A single flashcard with a prompt on the front and the answer on the back.
/// </summary>
public class Flashcard : Entity
{
    public Guid DeckId { get; set; }

    public Deck Deck { get; set; } = null!;

    public string Front { get; set; } = null!;

    public string Back { get; set; } = null!;
}
