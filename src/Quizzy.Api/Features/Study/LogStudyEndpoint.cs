using FastEndpoints;
using Quizzy.Application.Activities;
using Quizzy.Application.Common.Interfaces;

namespace Quizzy.Api.Features.Study;

/// <summary>Records that the current user studied today (e.g. after reviewing a deck).</summary>
public class LogStudyEndpoint : EndpointWithoutRequest
{
    private readonly IStudyActivityService _activity;
    private readonly ICurrentUserService _currentUser;

    public LogStudyEndpoint(IStudyActivityService activity, ICurrentUserService currentUser)
    {
        _activity = activity;
        _currentUser = currentUser;
    }

    public override void Configure()
    {
        Post("/study/log");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        await _activity.RecordForUserAsync(_currentUser.UserId, ct);
        await Send.NoContentAsync(ct);
    }
}
