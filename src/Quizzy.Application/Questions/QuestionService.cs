using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Exceptions;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;
using Quizzy.Domain.Enums;

namespace Quizzy.Application.Questions;

public class QuestionService : IQuestionService
{
    private const int MinOptions = 2;
    private const int MaxOptions = 10;

    private readonly IApplicationDbContext _db;
    private readonly ICurrentUserService _currentUser;

    public QuestionService(IApplicationDbContext db, ICurrentUserService currentUser)
    {
        _db = db;
        _currentUser = currentUser;
    }

    public async Task<IReadOnlyList<QuestionDto>> GetByQuizAsync(Guid quizId, CancellationToken cancellationToken = default)
    {
        await EnsureQuizIsOwnedAsync(quizId, cancellationToken);

        return await _db.Questions
            .Where(q => q.QuizId == quizId)
            .OrderBy(q => q.CreatedAt)
            .Select(q => new QuestionDto(
                q.Id,
                q.QuizId,
                q.Text,
                q.Type,
                q.Options
                    .OrderBy(o => o.CreatedAt)
                    .Select(o => new AnswerOptionDto(o.Id, o.Text, o.IsCorrect))
                    .ToList()))
            .ToListAsync(cancellationToken);
    }

    public async Task<QuestionDto> CreateAsync(Guid quizId, QuestionInput input, CancellationToken cancellationToken = default)
    {
        await EnsureQuizIsOwnedAsync(quizId, cancellationToken);
        ValidateComposition(input);

        var question = new Question
        {
            QuizId = quizId,
            Text = input.Text.Trim(),
            Type = input.Type,
            Options = input.Options
                .Select(o => new AnswerOption { Text = o.Text.Trim(), IsCorrect = o.IsCorrect })
                .ToList(),
        };

        _db.Questions.Add(question);
        await _db.SaveChangesAsync(cancellationToken);

        return ToDto(question);
    }

    public async Task<QuestionDto> UpdateAsync(Guid quizId, Guid questionId, QuestionInput input, CancellationToken cancellationToken = default)
    {
        await EnsureQuizIsOwnedAsync(quizId, cancellationToken);
        ValidateComposition(input);

        var question = await _db.Questions
            .Include(q => q.Options)
            .FirstOrDefaultAsync(q => q.Id == questionId && q.QuizId == quizId, cancellationToken)
            ?? throw new NotFoundException(nameof(Question), questionId);

        question.Text = input.Text.Trim();
        question.Type = input.Type;

        _db.AnswerOptions.RemoveRange(question.Options);
        question.Options = input.Options
            .Select(o => new AnswerOption { Text = o.Text.Trim(), IsCorrect = o.IsCorrect })
            .ToList();

        await _db.SaveChangesAsync(cancellationToken);

        return ToDto(question);
    }

    public async Task DeleteAsync(Guid quizId, Guid questionId, CancellationToken cancellationToken = default)
    {
        await EnsureQuizIsOwnedAsync(quizId, cancellationToken);

        var question = await _db.Questions
            .FirstOrDefaultAsync(q => q.Id == questionId && q.QuizId == quizId, cancellationToken)
            ?? throw new NotFoundException(nameof(Question), questionId);

        _db.Questions.Remove(question);
        await _db.SaveChangesAsync(cancellationToken);
    }

    private static void ValidateComposition(QuestionInput input)
    {
        var options = input.Options ?? new List<OptionInput>();

        if (options.Count < MinOptions || options.Count > MaxOptions)
        {
            throw new BadRequestException(
                $"A question must have between {MinOptions} and {MaxOptions} options.");
        }

        var correctCount = options.Count(o => o.IsCorrect);

        switch (input.Type)
        {
            case QuestionType.SingleChoice:
                if (correctCount != 1)
                {
                    throw new BadRequestException("A single-choice question must have exactly one correct option.");
                }

                break;

            case QuestionType.TrueFalse:
                if (options.Count != 2)
                {
                    throw new BadRequestException("A true/false question must have exactly two options.");
                }

                if (correctCount != 1)
                {
                    throw new BadRequestException("A true/false question must have exactly one correct option.");
                }

                break;

            case QuestionType.MultipleChoice:
                if (correctCount < 1)
                {
                    throw new BadRequestException("A multiple-choice question must have at least one correct option.");
                }

                break;
        }
    }

    private async Task EnsureQuizIsOwnedAsync(Guid quizId, CancellationToken cancellationToken)
    {
        var ownsQuiz = await _db.Quizzes.AnyAsync(
            q => q.Id == quizId && q.OwnerId == _currentUser.UserId,
            cancellationToken);

        if (!ownsQuiz)
        {
            throw new NotFoundException(nameof(Quiz), quizId);
        }
    }

    private static QuestionDto ToDto(Question q) => new(
        q.Id,
        q.QuizId,
        q.Text,
        q.Type,
        q.Options.Select(o => new AnswerOptionDto(o.Id, o.Text, o.IsCorrect)).ToList());
}
