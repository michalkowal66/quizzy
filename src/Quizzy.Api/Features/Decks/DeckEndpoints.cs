using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Decks;

namespace Quizzy.Api.Features.Decks;

public class CreateDeckRequest
{
    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public Guid? FolderId { get; set; }
}

public class CreateDeckValidator : Validator<CreateDeckRequest>
{
    public CreateDeckValidator()
    {
        RuleFor(x => x.Title)
            .NotEmpty()
            .MaximumLength(150);

        RuleFor(x => x.Description)
            .MaximumLength(1000);
    }
}

public class CreateDeckEndpoint : Endpoint<CreateDeckRequest, DeckDto>
{
    private readonly IDeckService _decks;

    public CreateDeckEndpoint(IDeckService decks) => _decks = decks;

    public override void Configure()
    {
        Post("/decks");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CreateDeckRequest req, CancellationToken ct)
    {
        var deck = await _decks.CreateAsync(
            new CreateDeckInput(req.Title, req.Description, req.FolderId), ct);
        await Send.ResponseAsync(deck, StatusCodes.Status201Created, ct);
    }
}

public class ListDecksEndpoint : EndpointWithoutRequest<IReadOnlyList<DeckDto>>
{
    private readonly IDeckService _decks;

    public ListDecksEndpoint(IDeckService decks) => _decks = decks;

    public override void Configure()
    {
        Get("/decks");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var decks = await _decks.GetAllAsync(ct);
        await Send.OkAsync(decks, ct);
    }
}

public class GetDeckEndpoint : EndpointWithoutRequest<DeckDto>
{
    private readonly IDeckService _decks;

    public GetDeckEndpoint(IDeckService decks) => _decks = decks;

    public override void Configure()
    {
        Get("/decks/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        var deck = await _decks.GetByIdAsync(id, ct);
        await Send.OkAsync(deck, ct);
    }
}

public class UpdateDeckRequest
{
    public Guid Id { get; set; }

    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public Guid? FolderId { get; set; }
}

public class UpdateDeckValidator : Validator<UpdateDeckRequest>
{
    public UpdateDeckValidator()
    {
        RuleFor(x => x.Title)
            .NotEmpty()
            .MaximumLength(150);

        RuleFor(x => x.Description)
            .MaximumLength(1000);
    }
}

public class UpdateDeckEndpoint : Endpoint<UpdateDeckRequest, DeckDto>
{
    private readonly IDeckService _decks;

    public UpdateDeckEndpoint(IDeckService decks) => _decks = decks;

    public override void Configure()
    {
        Put("/decks/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(UpdateDeckRequest req, CancellationToken ct)
    {
        var deck = await _decks.UpdateAsync(
            req.Id, new UpdateDeckInput(req.Title, req.Description, req.FolderId), ct);
        await Send.OkAsync(deck, ct);
    }
}

public class DeleteDeckEndpoint : EndpointWithoutRequest
{
    private readonly IDeckService _decks;

    public DeleteDeckEndpoint(IDeckService decks) => _decks = decks;

    public override void Configure()
    {
        Delete("/decks/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        await _decks.DeleteAsync(id, ct);
        await Send.NoContentAsync(ct);
    }
}
