using FastEndpoints;
using FluentValidation;
using Quizzy.Application.Users;

namespace Quizzy.Api.Features.Profile;

public class GetProfileEndpoint : EndpointWithoutRequest<ProfileDto>
{
    private readonly IProfileService _profile;

    public GetProfileEndpoint(IProfileService profile) => _profile = profile;

    public override void Configure()
    {
        Get("/me");
    }

    public override async Task HandleAsync(CancellationToken ct)
    {
        var profile = await _profile.GetAsync(ct);
        await Send.OkAsync(profile, ct);
    }
}

public class UpdateProfileRequest
{
    public string DisplayName { get; set; } = null!;
}

public class UpdateProfileValidator : Validator<UpdateProfileRequest>
{
    public UpdateProfileValidator()
    {
        RuleFor(x => x.DisplayName)
            .NotEmpty()
            .MaximumLength(100);
    }
}

public class UpdateProfileEndpoint : Endpoint<UpdateProfileRequest, ProfileDto>
{
    private readonly IProfileService _profile;

    public UpdateProfileEndpoint(IProfileService profile) => _profile = profile;

    public override void Configure()
    {
        Put("/me");
    }

    public override async Task HandleAsync(UpdateProfileRequest req, CancellationToken ct)
    {
        var profile = await _profile.UpdateDisplayNameAsync(req.DisplayName, ct);
        await Send.OkAsync(profile, ct);
    }
}
