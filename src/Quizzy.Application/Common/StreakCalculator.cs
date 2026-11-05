namespace Quizzy.Application.Common;

/// <summary>Current and longest run of consecutive active days.</summary>
public record StreakInfo(int Current, int Longest);

/// <summary>
/// Computes learning streaks from a set of active days, independent of persistence.
/// </summary>
public static class StreakCalculator
{
    public static StreakInfo Calculate(IEnumerable<DateOnly> activityDates, DateOnly today)
    {
        var days = activityDates.Distinct().OrderBy(d => d).ToList();

        if (days.Count == 0)
        {
            return new StreakInfo(0, 0);
        }

        var longest = 1;
        var run = 1;
        for (var i = 1; i < days.Count; i++)
        {
            run = days[i] == days[i - 1].AddDays(1) ? run + 1 : 1;
            longest = Math.Max(longest, run);
        }

        // The current streak stays alive while the most recent activity is today or yesterday.
        var latest = days[^1];
        var current = 0;
        if (latest == today || latest == today.AddDays(-1))
        {
            current = 1;
            for (var i = days.Count - 1; i > 0; i--)
            {
                if (days[i] == days[i - 1].AddDays(1))
                {
                    current++;
                }
                else
                {
                    break;
                }
            }
        }

        return new StreakInfo(current, longest);
    }
}
