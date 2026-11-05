using Quizzy.Application.Common;

namespace Quizzy.UnitTests.Common;

public class StreakCalculatorTests
{
    private static readonly DateOnly Today = new(2026, 11, 17);

    private static DateOnly D(int offsetFromToday) => Today.AddDays(offsetFromToday);

    [Fact]
    public void NoActivity_ReturnsZero()
    {
        var result = StreakCalculator.Calculate(Array.Empty<DateOnly>(), Today);

        Assert.Equal(new StreakInfo(0, 0), result);
    }

    [Fact]
    public void ActiveToday_CurrentIsOne()
    {
        var result = StreakCalculator.Calculate(new[] { Today }, Today);

        Assert.Equal(1, result.Current);
        Assert.Equal(1, result.Longest);
    }

    [Fact]
    public void ConsecutiveDaysEndingToday_CountsAll()
    {
        var result = StreakCalculator.Calculate(new[] { D(-2), D(-1), D(0) }, Today);

        Assert.Equal(3, result.Current);
        Assert.Equal(3, result.Longest);
    }

    [Fact]
    public void StreakEndingYesterday_IsStillAlive()
    {
        var result = StreakCalculator.Calculate(new[] { D(-2), D(-1) }, Today);

        Assert.Equal(2, result.Current);
    }

    [Fact]
    public void LastActivityOlderThanYesterday_CurrentIsZero()
    {
        var result = StreakCalculator.Calculate(new[] { D(-5), D(-4), D(-3) }, Today);

        Assert.Equal(0, result.Current);
        Assert.Equal(3, result.Longest);
    }

    [Fact]
    public void GapResetsCurrentButLongestKeepsMaxRun()
    {
        // A 3-day run, a gap, then a 2-day run ending today.
        var result = StreakCalculator.Calculate(
            new[] { D(-7), D(-6), D(-5), D(-1), D(0) }, Today);

        Assert.Equal(2, result.Current);
        Assert.Equal(3, result.Longest);
    }

    [Fact]
    public void DuplicateDays_AreIgnored()
    {
        var result = StreakCalculator.Calculate(new[] { D(0), D(0), D(-1) }, Today);

        Assert.Equal(2, result.Current);
        Assert.Equal(2, result.Longest);
    }
}
