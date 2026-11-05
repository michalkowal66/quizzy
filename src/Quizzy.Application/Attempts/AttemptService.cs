using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Activities;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Attempts;

public class AttemptService : IAttemptService
{
    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;
    private readonly IStudyActivityService _studyActivity;

    public AttemptService(
        IApplicationDbContext db,
        ICurrentUserService currentUser,
        IStudyActivityService studyActivity)
    {
        _db = db;
        _currentUser = currentUser;
        _studyActivity = studyActivity;
    }

    public async Task<AttemptResultDto> SubmitAsync(Guid quizId, SubmitAttemptInput input, CancellationToken cancellationToken = default)
    {
        var quiz = await _db.Quizzes
            .Include(q => q.Questions)
            .ThenInclude(q => q.Options)
            .FirstOrDefaultAsync(q => q.Id == quizId && q.OwnerId == _currentUser.UserId, cancellationToken)
            ?? throw new NotFoundException(nameof(Quiz), quizId);

        if (quiz.Questions.Count == 0)
        {
            throw new BadRequestException("A quiz without questions cannot be taken.");
        }

        var scorable = quiz.Questions
            .Select(q => new ScorableQuestion(
                q.Id,
                q.Options.Select(o => o.Id).ToList(),
                q.Options.Where(o => o.IsCorrect).Select(o => o.Id).ToList()))
            .ToList();

        var submitted = (input.Answers ?? new List<SubmitAnswerInput>())
            .GroupBy(a => a.QuestionId)
            .ToDictionary(
                g => g.Key,
                g => (IReadOnlyList<Guid>)g.Last().SelectedOptionIds.Distinct().ToList());

        var scored = AttemptScorer.Score(scorable, submitted);
        var score = scored.Count(s => s.IsCorrect);
        var maxScore = scorable.Count;

        var attempt = new QuizAttempt
        {
            QuizId = quizId,
            UserId = _currentUser.UserId,
            Score = score,
            MaxScore = maxScore,
            Answers = scored
                .Select(s => new AttemptAnswer
                {
                    QuestionId = s.QuestionId,
                    SelectedOptionIds = s.SelectedOptionIds.ToList(),
                    IsCorrect = s.IsCorrect,
                })
                .ToList(),
        };

        _db.QuizAttempts.Add(attempt);
        await _db.SaveChangesAsync(cancellationToken);

        await _studyActivity.RecordTodayAsync(cancellationToken);

        return new AttemptResultDto(
            attempt.Id,
            quizId,
            score,
            maxScore,
            Percentage(score, maxScore),
            attempt.CreatedAt,
            scored
                .Select(s => new AttemptAnswerResultDto(
                    s.QuestionId,
                    s.IsCorrect,
                    s.SelectedOptionIds,
                    s.CorrectOptionIds))
                .ToList());
    }

    public async Task<IReadOnlyList<AttemptSummaryDto>> GetByQuizAsync(Guid quizId, CancellationToken cancellationToken = default)
    {
        var ownsQuiz = await _db.Quizzes.AnyAsync(
            q => q.Id == quizId && q.OwnerId == _currentUser.UserId,
            cancellationToken);

        if (!ownsQuiz)
        {
            throw new NotFoundException(nameof(Quiz), quizId);
        }

        return await _db.QuizAttempts
            .Where(a => a.QuizId == quizId && a.UserId == _currentUser.UserId)
            .OrderByDescending(a => a.CreatedAt)
            .Select(a => new AttemptSummaryDto(
                a.Id,
                a.QuizId,
                a.Score,
                a.MaxScore,
                a.MaxScore == 0 ? 0 : Math.Round((double)a.Score / a.MaxScore * 100, 1),
                a.CreatedAt))
            .ToListAsync(cancellationToken);
    }

    private static double Percentage(int score, int maxScore) =>
        maxScore == 0 ? 0 : Math.Round((double)score / maxScore * 100, 1);
}
