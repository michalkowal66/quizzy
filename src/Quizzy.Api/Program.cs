using System.Text.Json.Serialization;
using FastEndpoints;
using Microsoft.EntityFrameworkCore;
using Quizzy.Api.Middleware;
using Quizzy.Api.Services;
using Quizzy.Application;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Infrastructure;
using Quizzy.Infrastructure.Persistence;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);
builder.Services.AddScoped<ICurrentUserService, CurrentUserService>();
builder.Services.AddFastEndpoints();

var app = builder.Build();

app.UseMiddleware<ExceptionHandlingMiddleware>();
app.UseFastEndpoints(c =>
    c.Serializer.Options.Converters.Add(new JsonStringEnumConverter()));

app.MapGet("/health", () => Results.Ok(new { status = "ok" }));

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<QuizzyDbContext>();
    await db.Database.MigrateAsync();
    await DbSeeder.SeedAsync(db);
}

app.Run();
