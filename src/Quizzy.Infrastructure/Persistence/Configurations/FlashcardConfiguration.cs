using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Quizzy.Domain.Entities;

namespace Quizzy.Infrastructure.Persistence.Configurations;

public class FlashcardConfiguration : IEntityTypeConfiguration<Flashcard>
{
    public void Configure(EntityTypeBuilder<Flashcard> builder)
    {
        builder.ToTable("flashcards");

        builder.HasKey(f => f.Id);

        builder.Property(f => f.Front)
            .HasMaxLength(1000)
            .IsRequired();

        builder.Property(f => f.Back)
            .HasMaxLength(1000)
            .IsRequired();

        builder.HasIndex(f => f.DeckId);
    }
}
