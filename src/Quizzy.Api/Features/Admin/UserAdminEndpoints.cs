using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Users;
using Quizzy.Domain.Enums;

namespace Quizzy.Api.Features.Admin;

public class ListUsersEndpoint : EndpointWithoutRequest<IReadOnlyList<UserSummaryDto>>
{
    private readonly IUserAdminService _users;

    public ListUsersEndpoint(IUserAdminService users) => _users = users;

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

public class GetUserEndpoint : EndpointWithoutRequest<UserDetailDto>
{
    private readonly IUserAdminService _users;

    public GetUserEndpoint(IUserAdminService users) => _users = users;

    public override void Configure()
    {
        Get("/admin/users/{id}");
        Roles("Admin");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        var user = await _users.GetByIdAsync(id, ct);
        await Send.OkAsync(user, ct);
    }
}

public class ChangeUserRoleRequest
{
    public Guid Id { get; set; }

    public UserRole Role { get; set; }
}

public class ChangeUserRoleValidator : Validator<ChangeUserRoleRequest>
{
    public ChangeUserRoleValidator()
    {
        RuleFor(x => x.Role)
            .IsInEnum();
    }
}

public class ChangeUserRoleEndpoint : Endpoint<ChangeUserRoleRequest, UserSummaryDto>
{
    private readonly IUserAdminService _users;

    public ChangeUserRoleEndpoint(IUserAdminService users) => _users = users;

    public override void Configure()
    {
        Put("/admin/users/{id}/role");
        Roles("Admin");
    }

    public override async Task HandleAsync(ChangeUserRoleRequest req, CancellationToken ct)
    {
        var user = await _users.ChangeRoleAsync(req.Id, req.Role, ct);
        await Send.OkAsync(user, ct);
    }
}

public class DeleteUserEndpoint : EndpointWithoutRequest
{
    private readonly IUserAdminService _users;

    public DeleteUserEndpoint(IUserAdminService users) => _users = users;

    public override void Configure()
    {
        Delete("/admin/users/{id}");
        Roles("Admin");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var id = Route<Guid>("id");
        await _users.DeleteAsync(id, ct);
        await Send.NoContentAsync(ct);
    }
}
