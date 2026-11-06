namespace Quizzy.Application.Activities;

public interface IStudyActivityService
{
    /// <summary>Records that the given user studied today, if not already recorded.</summary>
    Task RecordForUserAsync(Guid userId, CancellationToken cancellationToken = default);
}
