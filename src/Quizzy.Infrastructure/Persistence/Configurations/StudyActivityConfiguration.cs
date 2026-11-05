using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class StudyActivityConfiguration : IEntityTypeConfiguration<StudyActivity>
{
    public void Configure(EntityTypeBuilder<StudyActivity> builder)
    {
        builder.ToTable("study_activities");

        builder.HasKey(a => a.Id);

        builder.Property(a => a.Date)
            .HasColumnType("date");

        // At most one activity record per user per day.
        builder.HasIndex(a => new { a.UserId, a.Date })
            .IsUnique();

        builder.HasOne(a => a.User)
            .WithMany()
            .HasForeignKey(a => a.UserId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
