using FastEndpoints;
using Quizzy.Application.Users;

namespace Quizzy.Api.Features.Admin;

public class ListUsersEndpoint : EndpointWithoutRequest<IReadOnlyList<UserSummaryDto>>
{
    private readonly IUserDirectory _users;

    public ListUsersEndpoint(IUserDirectory users) => _users = users;

    public override void Configure()
    {
        Get("/admin/users");
        Roles("Admin");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var users = await _users.GetAllAsync(ct);
        await Send.OkAsync(users, ct);
    }
}
