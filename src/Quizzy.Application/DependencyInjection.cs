using Microsoft.Extensions.DependencyInjection;
using Quizzy.Application.Activities;
using Quizzy.Application.Attempts;
using Quizzy.Application.Auth;
using Quizzy.Application.Dashboard;
using Quizzy.Application.Decks;
using Quizzy.Application.Flashcards;
using Quizzy.Application.Folders;
using Quizzy.Application.Questions;
using Quizzy.Application.Quizzes;
using Quizzy.Application.Scheduling;
using Quizzy.Application.Users;

namespace Quizzy.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IUserAdminService, UserAdminService>();
        services.AddScoped<IProfileService, ProfileService>();
        services.AddScoped<IFolderService, FolderService>();
        services.AddScoped<IDeckService, DeckService>();
        services.AddScoped<IFlashcardService, FlashcardService>();
        services.AddScoped<IQuizService, QuizService>();
        services.AddScoped<IQuestionService, QuestionService>();
        services.AddScoped<IAttemptService, AttemptService>();
        services.AddScoped<IStudyActivityService, StudyActivityService>();
        services.AddScoped<IDashboardService, DashboardService>();
        services.AddScoped<IStreakReminderService, StreakReminderService>();

        return services;
    }
}
