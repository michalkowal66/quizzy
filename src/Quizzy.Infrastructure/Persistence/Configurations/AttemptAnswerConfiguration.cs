using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class AttemptAnswerConfiguration : IEntityTypeConfiguration<AttemptAnswer>
{
    public void Configure(EntityTypeBuilder<AttemptAnswer> builder)
    {
        builder.ToTable("attempt_answers");

        builder.HasKey(a => a.Id);

        // Stored as a PostgreSQL uuid[] column; the selected options are a snapshot
        // and are intentionally not enforced by a foreign key.
        builder.Property(a => a.SelectedOptionIds)
            .HasColumnType("uuid[]");

        builder.HasIndex(a => a.QuizAttemptId);
    }
}
