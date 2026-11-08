using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using Microsoft.Extensions.DependencyInjection;
using Quizzy.Application.Common.Interfaces;
using Quizzy.Application.Scheduling;
using Quizzy.Domain.Entities;

namespace Quizzy.IntegrationTests;

public class ApiIntegrationTests : IClassFixture<QuizzyApiFactory>
{
    private readonly QuizzyApiFactory _factory;

    public ApiIntegrationTests(QuizzyApiFactory factory) => _factory = factory;

    [Fact]
    public async Task ProtectedEndpoint_WithoutToken_ReturnsUnauthorized()
    {
        var client = _factory.CreateClient();

        var response = await client.GetAsync("/folders");

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Register_ThenCreateAndListDeck_Succeeds()
    {
        var client = _factory.CreateClient();
        await AuthenticateAsync(client);

        var create = await client.PostAsJsonAsync("/decks", new { title = "Biology" });
        Assert.Equal(HttpStatusCode.Created, create.StatusCode);

        var page = await client.GetFromJsonAsync<PagedResponse<DeckResponse>>("/decks");
        Assert.NotNull(page);
        Assert.Contains(page!.Items, d => d.Title == "Biology");
    }

    [Fact]
    public async Task Login_WithWrongPassword_ReturnsUnauthorized()
    {
        var client = _factory.CreateClient();
        var email = $"login_{Guid.NewGuid():N}@test.local";
        await client.PostAsJsonAsync("/auth/register",
            new { email, displayName = "Login", password = "Password1!" });

        var response = await client.PostAsJsonAsync("/auth/login",
            new { email, password = "WrongPassword" });

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Decks_AreIsolatedPerOwner()
    {
        var alice = _factory.CreateClient();
        await AuthenticateAsync(alice);
        var created = await (await alice.PostAsJsonAsync("/decks", new { title = "Alice Only" }))
            .Content.ReadFromJsonAsync<DeckResponse>();

        var bob = _factory.CreateClient();
        await AuthenticateAsync(bob);

        var bobPage = await bob.GetFromJsonAsync<PagedResponse<DeckResponse>>("/decks");
        Assert.DoesNotContain(bobPage!.Items, d => d.Id == created!.Id);

        var bobGet = await bob.GetAsync($"/decks/{created!.Id}");
        Assert.Equal(HttpStatusCode.NotFound, bobGet.StatusCode);
    }

    [Fact]
    public async Task QuizAttempt_IsScoredAndPersisted()
    {
        var client = _factory.CreateClient();
        await AuthenticateAsync(client);

        var quiz = await (await client.PostAsJsonAsync("/quizzes", new { title = "Scoring" }))
            .Content.ReadFromJsonAsync<IdResponse>();

        var question = await (await client.PostAsJsonAsync(
            $"/quizzes/{quiz!.Id}/questions",
            new
            {
                text = "2 + 2 = ?",
                type = "SingleChoice",
                options = new[]
                {
                    new { text = "4", isCorrect = true },
                    new { text = "5", isCorrect = false },
                },
            }))
            .Content.ReadFromJsonAsync<QuestionResponse>();

        var correctOption = question!.Options.Single(o => o.IsCorrect).Id;

        var attempt = await (await client.PostAsJsonAsync(
            $"/quizzes/{quiz.Id}/attempts",
            new { answers = new[] { new { questionId = question.Id, selectedOptionIds = new[] { correctOption } } } }))
            .Content.ReadFromJsonAsync<AttemptResponse>();

        Assert.Equal(1, attempt!.Score);
        Assert.Equal(1, attempt.MaxScore);

        var attempts = await client.GetFromJsonAsync<List<AttemptResponse>>($"/quizzes/{quiz.Id}/attempts");
        Assert.Single(attempts!);
    }

    [Fact]
    public async Task UpdatingQuestion_ReplacesItsOptions()
    {
        var client = _factory.CreateClient();
        await AuthenticateAsync(client);

        var quiz = await (await client.PostAsJsonAsync("/quizzes", new { title = "Editable" }))
            .Content.ReadFromJsonAsync<IdResponse>();

        var created = await (await client.PostAsJsonAsync(
            $"/quizzes/{quiz!.Id}/questions",
            new
            {
                text = "Original?",
                type = "SingleChoice",
                options = new[]
                {
                    new { text = "Old A", isCorrect = true },
                    new { text = "Old B", isCorrect = false },
                },
            }))
            .Content.ReadFromJsonAsync<QuestionResponse>();

        // Swap the whole option set: different count, texts and correct answers.
        var update = await client.PutAsJsonAsync(
            $"/quizzes/{quiz.Id}/questions/{created!.Id}",
            new
            {
                text = "Updated?",
                type = "MultipleChoice",
                options = new[]
                {
                    new { text = "New A", isCorrect = true },
                    new { text = "New B", isCorrect = true },
                    new { text = "New C", isCorrect = false },
                },
            });

        Assert.Equal(HttpStatusCode.OK, update.StatusCode);

        var questions = await client.GetFromJsonAsync<List<QuestionResponse>>(
            $"/quizzes/{quiz.Id}/questions");
        var question = Assert.Single(questions!);

        Assert.Equal("Updated?", question.Text);
        var optionTexts = question.Options.Select(o => o.Text).ToList();
        Assert.Equal(3, optionTexts.Count);
        Assert.Contains("New A", optionTexts);
        Assert.DoesNotContain("Old A", optionTexts);
        Assert.Equal(2, question.Options.Count(o => o.IsCorrect));
    }

    [Fact]
    public async Task AdminEndpoint_EnforcesRole()
    {
        var user = _factory.CreateClient();
        await AuthenticateAsync(user);
        var asUser = await user.GetAsync("/admin/users");
        Assert.Equal(HttpStatusCode.Forbidden, asUser.StatusCode);

        var admin = _factory.CreateClient();
        var login = await admin.PostAsJsonAsync("/auth/login",
            new { email = "admin@quizzy.local", password = "Admin123!" });
        var auth = await login.Content.ReadFromJsonAsync<AuthResponse>();
        admin.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", auth!.Token);

        var asAdmin = await admin.GetAsync("/admin/users");
        Assert.Equal(HttpStatusCode.OK, asAdmin.StatusCode);
    }

    [Fact]
    public async Task Decks_ArePaginated()
    {
        var client = _factory.CreateClient();
        await AuthenticateAsync(client);

        for (var i = 0; i < 3; i++)
        {
            await client.PostAsJsonAsync("/decks", new { title = $"Deck {i}" });
        }

        var page = await client.GetFromJsonAsync<PagedResponse<DeckResponse>>("/decks?page=1&pageSize=2");

        Assert.Equal(2, page!.Items.Count);
        Assert.Equal(3, page.TotalCount);
        Assert.Equal(2, page.TotalPages);
    }

    [Fact]
    public async Task CompletingQuiz_EventuallyRecordsStudyActivity()
    {
        var client = _factory.CreateClient();
        await AuthenticateAsync(client);

        var fresh = await client.GetFromJsonAsync<DashboardResponse>("/dashboard");
        Assert.False(fresh!.StudiedToday);

        var quiz = await (await client.PostAsJsonAsync("/quizzes", new { title = "Async" }))
            .Content.ReadFromJsonAsync<IdResponse>();
        var question = await (await client.PostAsJsonAsync(
            $"/quizzes/{quiz!.Id}/questions",
            new
            {
                text = "Capital of France?",
                type = "SingleChoice",
                options = new[]
                {
                    new { text = "Paris", isCorrect = true },
                    new { text = "Rome", isCorrect = false },
                },
            }))
            .Content.ReadFromJsonAsync<QuestionResponse>();
        var correct = question!.Options.Single(o => o.IsCorrect).Id;

        await client.PostAsJsonAsync(
            $"/quizzes/{quiz.Id}/attempts",
            new { answers = new[] { new { questionId = question.Id, selectedOptionIds = new[] { correct } } } });

        // The study activity is recorded by the QuizCompleted consumer, off the request thread.
        var studied = await WaitUntilAsync(async () =>
        {
            var dashboard = await client.GetFromJsonAsync<DashboardResponse>("/dashboard");
            return dashboard!.StudiedToday;
        });

        Assert.True(studied);
    }

    [Fact]
    public async Task StreakReminders_DispatchedForUsersActiveYesterdayOnly()
    {
        using var scope = _factory.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<IApplicationDbContext>();

        var today = DateOnly.FromDateTime(DateTime.UtcNow);

        // At risk: studied yesterday, not today.
        var atRisk = new User
        {
            Email = $"atrisk_{Guid.NewGuid():N}@test.local",
            DisplayName = "At Risk",
            PasswordHash = "x",
        };
        // Not at risk: already studied today.
        var safe = new User
        {
            Email = $"safe_{Guid.NewGuid():N}@test.local",
            DisplayName = "Safe",
            PasswordHash = "x",
        };
        db.Users.Add(atRisk);
        db.Users.Add(safe);
        db.StudyActivities.Add(new StudyActivity { UserId = atRisk.Id, Date = today.AddDays(-1) });
        db.StudyActivities.Add(new StudyActivity { UserId = safe.Id, Date = today });
        await db.SaveChangesAsync();

        var reminders = scope.ServiceProvider.GetRequiredService<IStreakReminderService>();
        var dispatched = await reminders.DispatchRemindersAsync();

        Assert.True(dispatched >= 1);
    }

    private static async Task<bool> WaitUntilAsync(Func<Task<bool>> condition)
    {
        for (var attempt = 0; attempt < 50; attempt++)
        {
            if (await condition())
            {
                return true;
            }

            await Task.Delay(100);
        }

        return false;
    }

    private async Task AuthenticateAsync(HttpClient client)
    {
        var email = $"user_{Guid.NewGuid():N}@test.local";
        var response = await client.PostAsJsonAsync("/auth/register",
            new { email, displayName = "Test User", password = "Password1!" });
        response.EnsureSuccessStatusCode();
        var auth = await response.Content.ReadFromJsonAsync<AuthResponse>();
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", auth!.Token);
    }

    private record AuthResponse(string Token, Guid UserId, string Email, string DisplayName, string Role);

    private record IdResponse(Guid Id);

    private record DeckResponse(Guid Id, string Title);

    private record OptionResponse(Guid Id, string Text, bool IsCorrect);

    private record QuestionResponse(Guid Id, string Text, List<OptionResponse> Options);

    private record AttemptResponse(int Score, int MaxScore, double Percentage);

    private record PagedResponse<T>(List<T> Items, int Page, int PageSize, int TotalCount, int TotalPages);

    private record DashboardResponse(int CurrentStreak, int LongestStreak, bool StudiedToday);
}
