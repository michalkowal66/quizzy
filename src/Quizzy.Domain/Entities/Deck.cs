using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A collection of flashcards owned by a user and optionally placed in a folder.
/// </summary>
public class Deck : Entity
{
    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public Guid OwnerId { get; set; }

    public User Owner { get; set; } = null!;

    public Guid? FolderId { get; set; }

    public Folder? Folder { get; set; }

    public DateTime? UpdatedAt { get; set; }

    public ICollection<Flashcard> Flashcards { get; set; } = new List<Flashcard>();
}
