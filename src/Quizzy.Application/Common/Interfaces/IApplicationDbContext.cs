using Microsoft.EntityFrameworkCore;
using Quizzy.Domain.Entities;

namespace Quizzy.Application.Common.Interfaces;

/// <summary>
/// Abstraction over the persistence context used by application services.
/// </summary>
public interface IApplicationDbContext
{
    DbSet<User> Users { get; }

    DbSet<Folder> Folders { get; }

    DbSet<Deck> Decks { get; }

    DbSet<Flashcard> Flashcards { get; }

    DbSet<Quiz> Quizzes { get; }

    DbSet<Question> Questions { get; }

    DbSet<AnswerOption> AnswerOptions { get; }

    DbSet<QuizAttempt> QuizAttempts { get; }

    DbSet<AttemptAnswer> AttemptAnswers { get; }

    Task<int> SaveChangesAsync(CancellationToken cancellationToken = default);
}
