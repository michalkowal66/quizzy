using Microsoft.Extensions.DependencyInjection;
using Quizzy.Application.Decks;
using Quizzy.Application.Flashcards;
using Quizzy.Application.Folders;

namespace Quizzy.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IFolderService, FolderService>();
        services.AddScoped<IDeckService, DeckService>();
        services.AddScoped<IFlashcardService, FlashcardService>();

        return services;
    }
}
