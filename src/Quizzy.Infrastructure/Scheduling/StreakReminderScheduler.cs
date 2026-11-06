using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Quizzy.Application.Scheduling;

namespace Quizzy.Infrastructure.Scheduling;

/// <summary>
/// Runs once a day at a configured hour and dispatches reminders to users whose
/// streak is about to break.
/// </summary>
public class StreakReminderScheduler : BackgroundService
{
    private readonly IServiceScopeFactory _scopeFactory;
    private readonly ILogger<StreakReminderScheduler> _logger;
    private readonly bool _enabled;
    private readonly int _runHourUtc;

    public StreakReminderScheduler(
        IServiceScopeFactory scopeFactory,
        IConfiguration configuration,
        ILogger<StreakReminderScheduler> logger)
    {
        _scopeFactory = scopeFactory;
        _logger = logger;
        _enabled = configuration.GetValue("Scheduler:Enabled", true);
        _runHourUtc = Math.Clamp(configuration.GetValue("Scheduler:DailyRunHourUtc", 6), 0, 23);
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        if (!_enabled)
        {
            _logger.LogInformation("Streak reminder scheduler is disabled.");
            return;
        }

        while (!stoppingToken.IsCancellationRequested)
        {
            var delay = TimeUntilNextRun(DateTime.UtcNow);

            try
            {
                await Task.Delay(delay, stoppingToken);
            }
            catch (OperationCanceledException)
            {
                break;
            }

            try
            {
                using var scope = _scopeFactory.CreateScope();
                var reminders = scope.ServiceProvider.GetRequiredService<IStreakReminderService>();
                var dispatched = await reminders.DispatchRemindersAsync(stoppingToken);

                _logger.LogInformation("Streak reminder run dispatched {Count} reminder(s).", dispatched);
            }
            catch (Exception ex) when (!stoppingToken.IsCancellationRequested)
            {
                _logger.LogError(ex, "Streak reminder run failed.");
            }
        }
    }

    private TimeSpan TimeUntilNextRun(DateTime utcNow)
    {
        var next = new DateTime(utcNow.Year, utcNow.Month, utcNow.Day, _runHourUtc, 0, 0, DateTimeKind.Utc);
        if (next <= utcNow)
        {
            next = next.AddDays(1);
        }

        return next - utcNow;
    }
}
