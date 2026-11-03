namespace Quizzy.Application.Folders;

public interface IFolderService
{
    Task<IReadOnlyList<FolderDto>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<FolderDto> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

    Task<FolderDto> CreateAsync(string name, CancellationToken cancellationToken = default);

    Task<FolderDto> RenameAsync(Guid id, string name, CancellationToken cancellationToken = default);

    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);
}
