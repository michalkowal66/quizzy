using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class FolderConfiguration : IEntityTypeConfiguration<Folder>
{
    public void Configure(EntityTypeBuilder<Folder> builder)
    {
        builder.ToTable("folders");

        builder.HasKey(f => f.Id);

        builder.Property(f => f.Name)
            .HasMaxLength(100)
            .IsRequired();

        builder.HasIndex(f => new { f.OwnerId, f.Name })
            .IsUnique();

        builder.HasMany(f => f.Decks)
            .WithOne(d => d.Folder)
            .HasForeignKey(d => d.FolderId)
            .OnDelete(DeleteBehavior.SetNull);
    }
}
