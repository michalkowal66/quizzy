using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// A test made up of questions that a user can take to check their knowledge.
/// </summary>
public class Quiz : Entity
{
    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public Guid OwnerId { get; set; }

    public User Owner { get; set; } = null!;

    public Guid? FolderId { get; set; }

    public Folder? Folder { get; set; }

    public DateTime? UpdatedAt { get; set; }

    public ICollection<Question> Questions { get; set; } = new List<Question>();
}
