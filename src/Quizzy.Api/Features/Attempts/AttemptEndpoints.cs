using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Attempts;

namespace Quizzy.Api.Features.Attempts;

public class AnswerRequest
{
    public Guid QuestionId { get; set; }

    public List<Guid> SelectedOptionIds { get; set; } = new();
}

public class SubmitAttemptRequest
{
    public Guid QuizId { get; set; }

    public List<AnswerRequest> Answers { get; set; } = new();
}

public class SubmitAttemptValidator : Validator<SubmitAttemptRequest>
{
    public SubmitAttemptValidator()
    {
        RuleFor(x => x.Answers)
            .NotEmpty();

        RuleForEach(x => x.Answers).ChildRules(a =>
        {
            a.RuleFor(p => p.QuestionId)
                .NotEmpty();
        });
    }
}

public class SubmitAttemptEndpoint : Endpoint<SubmitAttemptRequest, AttemptResultDto>
{
    private readonly IAttemptService _attempts;

    public SubmitAttemptEndpoint(IAttemptService attempts) => _attempts = attempts;

    public override void Configure()
    {
        Post("/quizzes/{quizId}/attempts");
    }

    public override async Task HandleAsync(SubmitAttemptRequest req, CancellationToken ct)
    {
        var input = new SubmitAttemptInput(
            req.Answers
                .Select(a => new SubmitAnswerInput(a.QuestionId, a.SelectedOptionIds))
                .ToList());

        var result = await _attempts.SubmitAsync(req.QuizId, input, ct);
        await Send.ResponseAsync(result, StatusCodes.Status201Created, ct);
    }
}

public class ListAttemptsEndpoint : EndpointWithoutRequest<IReadOnlyList<AttemptSummaryDto>>
{
    private readonly IAttemptService _attempts;

    public ListAttemptsEndpoint(IAttemptService attempts) => _attempts = attempts;

    public override void Configure()
    {
        Get("/quizzes/{quizId}/attempts");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var quizId = Route<Guid>("quizId");
        var attempts = await _attempts.GetByQuizAsync(quizId, ct);
        await Send.OkAsync(attempts, ct);
    }
}
