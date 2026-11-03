using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class AnswerOptionConfiguration : IEntityTypeConfiguration<AnswerOption>
{
    public void Configure(EntityTypeBuilder<AnswerOption> builder)
    {
        builder.ToTable("answer_options");

        builder.HasKey(o => o.Id);

        builder.Property(o => o.Text)
            .HasMaxLength(1000)
            .IsRequired();

        builder.HasIndex(o => o.QuestionId);
    }
}
