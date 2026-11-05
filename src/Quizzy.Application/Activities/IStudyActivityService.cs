namespace Quizzy.Application.Activities;

public interface IStudyActivityService
{
    /// <summary>Records that the current user studied today, if not already recorded.</summary>
    Task RecordTodayAsync(CancellationToken cancellationToken = default);
}
