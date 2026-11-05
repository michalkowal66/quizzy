using FastEndpoints;
using Quizzy.Application.Dashboard;

namespace Quizzy.Api.Features.Dashboard;

public class DashboardEndpoint : EndpointWithoutRequest<DashboardDto>
{
    private readonly IDashboardService _dashboard;

    public DashboardEndpoint(IDashboardService dashboard) => _dashboard = dashboard;

    public override void Configure()
    {
        Get("/dashboard");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var dashboard = await _dashboard.GetAsync(ct);
        await Send.OkAsync(dashboard, ct);
    }
}
