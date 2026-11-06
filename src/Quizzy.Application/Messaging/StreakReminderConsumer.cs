using MassTransit;
using Microsoft.Extensions.Logging;

namespace Quizzy.Application.Messaging;

/// <summary>
/// Delivers a streak reminder. For now it logs the notification; a real transport
/// (e.g. email) can be plugged in without touching the scheduler or publisher.
/// </summary>
public class StreakReminderConsumer : IConsumer<StreakReminderDue>
{
    private readonly ILogger<StreakReminderConsumer> _logger;

    public StreakReminderConsumer(ILogger<StreakReminderConsumer> logger) => _logger = logger;

    public Task Consume(ConsumeContext<StreakReminderDue> context)
    {
        var message = context.Message;

        _logger.LogInformation(
            "Reminder for {DisplayName} <{Email}>: keep your {CurrentStreak}-day streak alive today.",
            message.DisplayName,
            message.Email,
            message.CurrentStreak);

        return Task.CompletedTask;
    }
}
