using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Flashcards;

namespace Quizzy.Api.Features.Flashcards;

public class CreateFlashcardRequest
{
    public Guid DeckId { get; set; }

    public string Front { get; set; } = null!;

    public string Back { get; set; } = null!;
}

public class CreateFlashcardValidator : Validator<CreateFlashcardRequest>
{
    public CreateFlashcardValidator()
    {
        RuleFor(x => x.Front)
            .NotEmpty()
            .MaximumLength(1000);

        RuleFor(x => x.Back)
            .NotEmpty()
            .MaximumLength(1000);
    }
}

public class CreateFlashcardEndpoint : Endpoint<CreateFlashcardRequest, FlashcardDto>
{
    private readonly IFlashcardService _flashcards;

    public CreateFlashcardEndpoint(IFlashcardService flashcards) => _flashcards = flashcards;

    public override void Configure()
    {
        Post("/decks/{deckId}/flashcards");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CreateFlashcardRequest req, CancellationToken ct)
    {
        var flashcard = await _flashcards.CreateAsync(req.DeckId, req.Front, req.Back, ct);
        await Send.ResponseAsync(flashcard, StatusCodes.Status201Created, ct);
    }
}

public class ListFlashcardsEndpoint : EndpointWithoutRequest<IReadOnlyList<FlashcardDto>>
{
    private readonly IFlashcardService _flashcards;

    public ListFlashcardsEndpoint(IFlashcardService flashcards) => _flashcards = flashcards;

    public override void Configure()
    {
        Get("/decks/{deckId}/flashcards");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var deckId = Route<Guid>("deckId");
        var flashcards = await _flashcards.GetByDeckAsync(deckId, ct);
        await Send.OkAsync(flashcards, ct);
    }
}

public class UpdateFlashcardRequest
{
    public Guid DeckId { get; set; }

    public Guid Id { get; set; }

    public string Front { get; set; } = null!;

    public string Back { get; set; } = null!;
}

public class UpdateFlashcardValidator : Validator<UpdateFlashcardRequest>
{
    public UpdateFlashcardValidator()
    {
        RuleFor(x => x.Front)
            .NotEmpty()
            .MaximumLength(1000);

        RuleFor(x => x.Back)
            .NotEmpty()
            .MaximumLength(1000);
    }
}

public class UpdateFlashcardEndpoint : Endpoint<UpdateFlashcardRequest, FlashcardDto>
{
    private readonly IFlashcardService _flashcards;

    public UpdateFlashcardEndpoint(IFlashcardService flashcards) => _flashcards = flashcards;

    public override void Configure()
    {
        Put("/decks/{deckId}/flashcards/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(UpdateFlashcardRequest req, CancellationToken ct)
    {
        var flashcard = await _flashcards.UpdateAsync(req.DeckId, req.Id, req.Front, req.Back, ct);
        await Send.OkAsync(flashcard, ct);
    }
}

public class DeleteFlashcardEndpoint : EndpointWithoutRequest
{
    private readonly IFlashcardService _flashcards;

    public DeleteFlashcardEndpoint(IFlashcardService flashcards) => _flashcards = flashcards;

    public override void Configure()
    {
        Delete("/decks/{deckId}/flashcards/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var deckId = Route<Guid>("deckId");
        var id = Route<Guid>("id");
        await _flashcards.DeleteAsync(deckId, id, ct);
        await Send.NoContentAsync(ct);
    }
}
