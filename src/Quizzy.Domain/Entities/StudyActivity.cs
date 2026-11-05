using Quizzy.Domain.Common;

namespace Quizzy.Domain.Entities;

/// <summary>
/// Marks that a user studied on a particular calendar day. Used to compute learning streaks.
/// </summary>
public class StudyActivity : Entity
{
    public Guid UserId { get; set; }

    public User User { get; set; } = null!;

    /// <summary>The UTC calendar day the activity happened on.</summary>
    public DateOnly Date { get; set; }
}
