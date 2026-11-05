using FastEndpoints;
using Quizzy.Application.Activities;

namespace Quizzy.Api.Features.Study;

/// <summary>Records that the current user studied today (e.g. after reviewing a deck).</summary>
public class LogStudyEndpoint : EndpointWithoutRequest
{
    private readonly IStudyActivityService _activity;

    public LogStudyEndpoint(IStudyActivityService activity) => _activity = activity;

    public override void Configure()
    {
        Post("/study/log");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        await _activity.RecordTodayAsync(ct);
        await Send.NoContentAsync(ct);
    }
}
