using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Activities;

public class StudyActivityService : IStudyActivityService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public StudyActivityService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task RecordTodayAsync(CancellationToken cancellationToken = default)
    {
        var userId = _currentUser.UserId;
        var today = DateOnly.FromDateTime(DateTime.UtcNow);

        var alreadyRecorded = await _db.StudyActivities
            .AnyAsync(a => a.UserId == userId && a.Date == today, cancellationToken);

        if (alreadyRecorded)
        {
            return;
        }

        _db.StudyActivities.Add(new StudyActivity { UserId = userId, Date = today });
        await _db.SaveChangesAsync(cancellationToken);
    }
}
