using Microsoft.EntityFrameworkCore;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence;

public class QuizzyDbContext : DbContext
{
    public QuizzyDbContext(DbContextOptions<QuizzyDbContext> options)
        : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();

    public DbSet<Folder> Folders => Set<Folder>();

    public DbSet<Deck> Decks => Set<Deck>();

    public DbSet<Flashcard> Flashcards => Set<Flashcard>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(QuizzyDbContext).Assembly);
        base.OnModelCreating(modelBuilder);
    }
}
