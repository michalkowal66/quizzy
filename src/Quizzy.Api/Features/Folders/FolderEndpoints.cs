using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Folders;

namespace Quizzy.Api.Features.Folders;

public class CreateFolderRequest
{
    public string Name { get; set; } = null!;
}

public class CreateFolderValidator : Validator<CreateFolderRequest>
{
    public CreateFolderValidator()
    {
        RuleFor(x => x.Name)
            .NotEmpty()
            .MaximumLength(100);
    }
}

public class CreateFolderEndpoint : Endpoint<CreateFolderRequest, FolderDto>
{
    private readonly IFolderService _folders;

    public CreateFolderEndpoint(IFolderService folders) => _folders = folders;

    public override void Configure()
    {
        Post("/folders");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CreateFolderRequest req, CancellationToken ct)
    {
        var folder = await _folders.CreateAsync(req.Name, ct);
        await Send.ResponseAsync(folder, StatusCodes.Status201Created, ct);
    }
}

public class ListFoldersEndpoint : EndpointWithoutRequest<IReadOnlyList<FolderDto>>
{
    private readonly IFolderService _folders;

    public ListFoldersEndpoint(IFolderService folders) => _folders = folders;

    public override void Configure()
    {
        Get("/folders");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var folders = await _folders.GetAllAsync(ct);
        await Send.OkAsync(folders, ct);
    }
}

public class GetFolderEndpoint : EndpointWithoutRequest<FolderDto>
{
    private readonly IFolderService _folders;

    public GetFolderEndpoint(IFolderService folders) => _folders = folders;

    public override void Configure()
    {
        Get("/folders/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        var folder = await _folders.GetByIdAsync(id, ct);
        await Send.OkAsync(folder, ct);
    }
}

public class RenameFolderRequest
{
    public Guid Id { get; set; }

    public string Name { get; set; } = null!;
}

public class RenameFolderValidator : Validator<RenameFolderRequest>
{
    public RenameFolderValidator()
    {
        RuleFor(x => x.Name)
            .NotEmpty()
            .MaximumLength(100);
    }
}

public class RenameFolderEndpoint : Endpoint<RenameFolderRequest, FolderDto>
{
    private readonly IFolderService _folders;

    public RenameFolderEndpoint(IFolderService folders) => _folders = folders;

    public override void Configure()
    {
        Put("/folders/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(RenameFolderRequest req, CancellationToken ct)
    {
        var folder = await _folders.RenameAsync(req.Id, req.Name, ct);
        await Send.OkAsync(folder, ct);
    }
}

public class DeleteFolderEndpoint : EndpointWithoutRequest
{
    private readonly IFolderService _folders;

    public DeleteFolderEndpoint(IFolderService folders) => _folders = folders;

    public override void Configure()
    {
        Delete("/folders/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        await _folders.DeleteAsync(id, ct);
        await Send.NoContentAsync(ct);
    }
}
