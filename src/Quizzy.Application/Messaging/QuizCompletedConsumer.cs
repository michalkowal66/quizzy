using MassTransit;
using Microsoft.Extensions.Logging;
using Quizzy.Application.Activities;

namespace Quizzy.Application.Messaging;

/// <summary>
/// Handles a completed quiz asynchronously: records the user's study activity
/// and emits a notification for the result.
/// </summary>
public class QuizCompletedConsumer : IConsumer<QuizCompleted>
{
    private readonly IStudyActivityService _activity;
    private readonly ILogger<QuizCompletedConsumer> _logger;

    public QuizCompletedConsumer(IStudyActivityService activity, ILogger<QuizCompletedConsumer> logger)
    {
        _activity = activity;
        _logger = logger;
    }

    public async Task Consume(ConsumeContext<QuizCompleted> context)
    {
        var message = context.Message;

        await _activity.RecordForUserAsync(message.UserId, context.CancellationToken);

        _logger.LogInformation(
            "Quiz {QuizId} completed by user {UserId} with score {Score}/{MaxScore}.",
            message.QuizId,
            message.UserId,
            message.Score,
            message.MaxScore);
    }
}
