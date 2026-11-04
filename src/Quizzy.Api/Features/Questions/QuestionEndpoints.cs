using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Questions;
using Quizzy.Domain.Enums;

namespace Quizzy.Api.Features.Questions;

public class OptionRequest
{
    public string Text { get; set; } = null!;

    public bool IsCorrect { get; set; }
}

public class CreateQuestionRequest
{
    public Guid QuizId { get; set; }

    public string Text { get; set; } = null!;

    public QuestionType Type { get; set; }

    public List<OptionRequest> Options { get; set; } = new();
}

public class CreateQuestionValidator : Validator<CreateQuestionRequest>
{
    public CreateQuestionValidator()
    {
        RuleFor(x => x.Text)
            .NotEmpty()
            .MaximumLength(2000);

        RuleFor(x => x.Options)
            .NotEmpty();

        RuleForEach(x => x.Options).ChildRules(o =>
        {
            o.RuleFor(p => p.Text)
                .NotEmpty()
                .MaximumLength(1000);
        });
    }
}

public class CreateQuestionEndpoint : Endpoint<CreateQuestionRequest, QuestionDto>
{
    private readonly IQuestionService _questions;

    public CreateQuestionEndpoint(IQuestionService questions) => _questions = questions;

    public override void Configure()
    {
        Post("/quizzes/{quizId}/questions");
    }

    public override async Task HandleAsync(CreateQuestionRequest req, CancellationToken ct)
    {
        var question = await _questions.CreateAsync(req.QuizId, ToInput(req.Text, req.Type, req.Options), ct);
        await Send.ResponseAsync(question, StatusCodes.Status201Created, ct);
    }

    internal static QuestionInput ToInput(string text, QuestionType type, List<OptionRequest> options) =>
        new(text, type, options.Select(o => new OptionInput(o.Text, o.IsCorrect)).ToList());
}

public class ListQuestionsEndpoint : EndpointWithoutRequest<IReadOnlyList<QuestionDto>>
{
    private readonly IQuestionService _questions;

    public ListQuestionsEndpoint(IQuestionService questions) => _questions = questions;

    public override void Configure()
    {
        Get("/quizzes/{quizId}/questions");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var quizId = Route<Guid>("quizId");
        var questions = await _questions.GetByQuizAsync(quizId, ct);
        await Send.OkAsync(questions, ct);
    }
}

public class UpdateQuestionRequest
{
    public Guid QuizId { get; set; }

    public Guid Id { get; set; }

    public string Text { get; set; } = null!;

    public QuestionType Type { get; set; }

    public List<OptionRequest> Options { get; set; } = new();
}

public class UpdateQuestionValidator : Validator<UpdateQuestionRequest>
{
    public UpdateQuestionValidator()
    {
        RuleFor(x => x.Text)
            .NotEmpty()
            .MaximumLength(2000);

        RuleFor(x => x.Options)
            .NotEmpty();

        RuleForEach(x => x.Options).ChildRules(o =>
        {
            o.RuleFor(p => p.Text)
                .NotEmpty()
                .MaximumLength(1000);
        });
    }
}

public class UpdateQuestionEndpoint : Endpoint<UpdateQuestionRequest, QuestionDto>
{
    private readonly IQuestionService _questions;

    public UpdateQuestionEndpoint(IQuestionService questions) => _questions = questions;

    public override void Configure()
    {
        Put("/quizzes/{quizId}/questions/{id}");
    }

    public override async Task HandleAsync(UpdateQuestionRequest req, CancellationToken ct)
    {
        var question = await _questions.UpdateAsync(
            req.QuizId, req.Id, CreateQuestionEndpoint.ToInput(req.Text, req.Type, req.Options), ct);
        await Send.OkAsync(question, ct);
    }
}

public class DeleteQuestionEndpoint : EndpointWithoutRequest
{
    private readonly IQuestionService _questions;

    public DeleteQuestionEndpoint(IQuestionService questions) => _questions = questions;

    public override void Configure()
    {
        Delete("/quizzes/{quizId}/questions/{id}");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var quizId = Route<Guid>("quizId");
        var id = Route<Guid>("id");
        await _questions.DeleteAsync(quizId, id, ct);
        await Send.NoContentAsync(ct);
    }
}
