using Microsoft.EntityFrameworkCore;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence;

public class QuizzyDbContext : DbContext, IApplicationDbContext
{
    public QuizzyDbContext(DbContextOptions<QuizzyDbContext> options)
        : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();

    public DbSet<Folder> Folders => Set<Folder>();

    public DbSet<Deck> Decks => Set<Deck>();

    public DbSet<Flashcard> Flashcards => Set<Flashcard>();

    public DbSet<Quiz> Quizzes => Set<Quiz>();

    public DbSet<Question> Questions => Set<Question>();

    public DbSet<AnswerOption> AnswerOptions => Set<AnswerOption>();

    public DbSet<QuizAttempt> QuizAttempts => Set<QuizAttempt>();

    public DbSet<AttemptAnswer> AttemptAnswers => Set<AttemptAnswer>();

    public DbSet<StudyActivity> StudyActivities => Set<StudyActivity>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(QuizzyDbContext).Assembly);
        base.OnModelCreating(modelBuilder);
    }
}
