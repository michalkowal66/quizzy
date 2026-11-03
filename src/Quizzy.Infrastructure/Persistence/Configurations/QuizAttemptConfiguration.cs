using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class QuizAttemptConfiguration : IEntityTypeConfiguration<QuizAttempt>
{
    public void Configure(EntityTypeBuilder<QuizAttempt> builder)
    {
        builder.ToTable("quiz_attempts");

        builder.HasKey(a => a.Id);

        builder.HasIndex(a => new { a.UserId, a.QuizId });

        builder.HasOne(a => a.Quiz)
            .WithMany()
            .HasForeignKey(a => a.QuizId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(a => a.User)
            .WithMany()
            .HasForeignKey(a => a.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasMany(a => a.Answers)
            .WithOne(x => x.QuizAttempt)
            .HasForeignKey(x => x.QuizAttemptId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
