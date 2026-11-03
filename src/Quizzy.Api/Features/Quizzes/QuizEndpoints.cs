using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Quizzes;

namespace Quizzy.Api.Features.Quizzes;

public class CreateQuizRequest
{
    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public Guid? FolderId { get; set; }
}

public class CreateQuizValidator : Validator<CreateQuizRequest>
{
    public CreateQuizValidator()
    {
        RuleFor(x => x.Title)
            .NotEmpty()
            .MaximumLength(150);

        RuleFor(x => x.Description)
            .MaximumLength(1000);
    }
}

public class CreateQuizEndpoint : Endpoint<CreateQuizRequest, QuizDto>
{
    private readonly IQuizService _quizzes;

    public CreateQuizEndpoint(IQuizService quizzes) => _quizzes = quizzes;

    public override void Configure()
    {
        Post("/quizzes");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CreateQuizRequest req, CancellationToken ct)
    {
        var quiz = await _quizzes.CreateAsync(
            new CreateQuizInput(req.Title, req.Description, req.FolderId), ct);
        await Send.ResponseAsync(quiz, StatusCodes.Status201Created, ct);
    }
}

public class ListQuizzesEndpoint : EndpointWithoutRequest<IReadOnlyList<QuizDto>>
{
    private readonly IQuizService _quizzes;

    public ListQuizzesEndpoint(IQuizService quizzes) => _quizzes = quizzes;

    public override void Configure()
    {
        Get("/quizzes");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var quizzes = await _quizzes.GetAllAsync(ct);
        await Send.OkAsync(quizzes, ct);
    }
}

public class GetQuizEndpoint : EndpointWithoutRequest<QuizDto>
{
    private readonly IQuizService _quizzes;

    public GetQuizEndpoint(IQuizService quizzes) => _quizzes = quizzes;

    public override void Configure()
    {
        Get("/quizzes/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        var quiz = await _quizzes.GetByIdAsync(id, ct);
        await Send.OkAsync(quiz, ct);
    }
}

public class UpdateQuizRequest
{
    public Guid Id { get; set; }

    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public Guid? FolderId { get; set; }
}

public class UpdateQuizValidator : Validator<UpdateQuizRequest>
{
    public UpdateQuizValidator()
    {
        RuleFor(x => x.Title)
            .NotEmpty()
            .MaximumLength(150);

        RuleFor(x => x.Description)
            .MaximumLength(1000);
    }
}

public class UpdateQuizEndpoint : Endpoint<UpdateQuizRequest, QuizDto>
{
    private readonly IQuizService _quizzes;

    public UpdateQuizEndpoint(IQuizService quizzes) => _quizzes = quizzes;

    public override void Configure()
    {
        Put("/quizzes/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(UpdateQuizRequest req, CancellationToken ct)
    {
        var quiz = await _quizzes.UpdateAsync(
            req.Id, new UpdateQuizInput(req.Title, req.Description, req.FolderId), ct);
        await Send.OkAsync(quiz, ct);
    }
}

public class DeleteQuizEndpoint : EndpointWithoutRequest
{
    private readonly IQuizService _quizzes;

    public DeleteQuizEndpoint(IQuizService quizzes) => _quizzes = quizzes;

    public override void Configure()
    {
        Delete("/quizzes/{id}");
        AllowAnonymous();
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        await _quizzes.DeleteAsync(id, ct);
        await Send.NoContentAsync(ct);
    }
}
