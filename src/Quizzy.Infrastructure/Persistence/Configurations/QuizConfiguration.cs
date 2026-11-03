using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class QuizConfiguration : IEntityTypeConfiguration<Quiz>
{
    public void Configure(EntityTypeBuilder<Quiz> builder)
    {
        builder.ToTable("quizzes");

        builder.HasKey(q => q.Id);

        builder.Property(q => q.Title)
            .HasMaxLength(150)
            .IsRequired();

        builder.Property(q => q.Description)
            .HasMaxLength(1000);

        builder.HasIndex(q => q.OwnerId);

        builder.HasOne(q => q.Owner)
            .WithMany()
            .HasForeignKey(q => q.OwnerId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(q => q.Folder)
            .WithMany()
            .HasForeignKey(q => q.FolderId)
            .OnDelete(DeleteBehavior.SetNull);

        builder.HasMany(q => q.Questions)
            .WithOne(x => x.Quiz)
            .HasForeignKey(x => x.QuizId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
