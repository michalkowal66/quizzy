namespace Quizzy.Application.Folders;

public record FolderDto(Guid Id, string Name, int DeckCount, DateTime CreatedAt);
