using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Activities;

public class StudyActivityService : IStudyActivityService
{
    private readonly IApplicationDbContext _db;

    public StudyActivityService(IApplicationDbContext db) => _db = db;

    public async Task RecordForUserAsync(Guid userId, CancellationToken cancellationToken = default)
    {
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
