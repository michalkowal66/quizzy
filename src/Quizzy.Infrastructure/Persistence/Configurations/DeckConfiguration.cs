using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class DeckConfiguration : IEntityTypeConfiguration<Deck>
{
    public void Configure(EntityTypeBuilder<Deck> builder)
    {
        builder.ToTable("decks");

        builder.HasKey(d => d.Id);

        builder.Property(d => d.Title)
            .HasMaxLength(150)
            .IsRequired();

        builder.Property(d => d.Description)
            .HasMaxLength(1000);

        builder.HasIndex(d => d.OwnerId);

        builder.HasMany(d => d.Flashcards)
            .WithOne(f => f.Deck)
            .HasForeignKey(f => f.DeckId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
