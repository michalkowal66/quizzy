using System.Text;
using MassTransit;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Application.Messaging;
using Quizzy.Infrastructure.Persistence;
using Quizzy.Infrastructure.Security;

namespace Quizzy.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("Postgres")
            ?? throw new InvalidOperationException(
                "Connection string 'Postgres' is not configured.");

        services.AddDbContext<QuizzyDbContext>(options =>
            options.UseNpgsql(connectionString));

        services.AddScoped<IApplicationDbContext>(sp => sp.GetRequiredService<QuizzyDbContext>());

        services.AddOptions<JwtSettings>()
            .Bind(configuration.GetSection(JwtSettings.SectionName))
            .Validate(
                s => !string.IsNullOrWhiteSpace(s.SigningKey),
                "Jwt:SigningKey must be configured (provide it via environment or user-secrets).")
            .Validate(
                s => string.IsNullOrEmpty(s.SigningKey) || Encoding.UTF8.GetByteCount(s.SigningKey) >= 32,
                "Jwt:SigningKey must be at least 32 bytes long.")
            .Validate(
                s => !string.IsNullOrWhiteSpace(s.Issuer) && !string.IsNullOrWhiteSpace(s.Audience),
                "Jwt:Issuer and Jwt:Audience must be configured.")
            .ValidateOnStart();

        services.AddSingleton<IPasswordHasher, PasswordHasher>();
        services.AddSingleton<IJwtTokenService, JwtTokenService>();

        services.AddMassTransit(bus =>
        {
            bus.AddConsumer<QuizCompletedConsumer>();

            var rabbitHost = configuration["RabbitMq:Host"];
            if (string.IsNullOrWhiteSpace(rabbitHost))
            {
                // No broker configured: run the bus in-process (local development and tests).
                bus.UsingInMemory((context, cfg) => cfg.ConfigureEndpoints(context));
            }
            else
            {
                bus.UsingRabbitMq((context, cfg) =>
                {
                    cfg.Host(rabbitHost, host =>
                    {
                        host.Username(configuration["RabbitMq:Username"] ?? "guest");
                        host.Password(configuration["RabbitMq:Password"] ?? "guest");
                    });
                    cfg.ConfigureEndpoints(context);
                });
            }
        });

        return services;
    }
}
