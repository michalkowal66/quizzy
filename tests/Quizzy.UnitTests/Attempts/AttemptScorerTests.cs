using Quizzy.Application.Attempts;

namespace Quizzy.UnitTests.Attempts;

public class AttemptScorerTests
{
    private static readonly Guid A = Guid.NewGuid();
    private static readonly Guid B = Guid.NewGuid();
    private static readonly Guid C = Guid.NewGuid();
    private static readonly Guid QId = Guid.NewGuid();

    private static ScorableQuestion Question(IReadOnlyList<Guid> correct) =>
        new(QId, new[] { A, B, C }, correct);

    private static IReadOnlyDictionary<Guid, IReadOnlyList<Guid>> Submit(params Guid[] selected) =>
        new Dictionary<Guid, IReadOnlyList<Guid>> { [QId] = selected };

    [Fact]
    public void SingleCorrectOption_Selected_IsCorrect()
    {
        var result = AttemptScorer.Score(new[] { Question(new[] { A }) }, Submit(A));

        Assert.True(result.Single().IsCorrect);
    }

    [Fact]
    public void SingleCorrectOption_WrongSelected_IsIncorrect()
    {
        var result = AttemptScorer.Score(new[] { Question(new[] { A }) }, Submit(B));

        Assert.False(result.Single().IsCorrect);
    }

    [Fact]
    public void MultipleCorrect_ExactMatch_IsCorrect()
    {
        var result = AttemptScorer.Score(new[] { Question(new[] { A, B }) }, Submit(A, B));

        Assert.True(result.Single().IsCorrect);
    }

    [Fact]
    public void MultipleCorrect_PartialSelection_IsIncorrect()
    {
        var result = AttemptScorer.Score(new[] { Question(new[] { A, B }) }, Submit(A));

        Assert.False(result.Single().IsCorrect);
    }

    [Fact]
    public void MultipleCorrect_ExtraSelection_IsIncorrect()
    {
        var result = AttemptScorer.Score(new[] { Question(new[] { A, B }) }, Submit(A, B, C));

        Assert.False(result.Single().IsCorrect);
    }

    [Fact]
    public void UnansweredQuestion_IsIncorrect()
    {
        var result = AttemptScorer.Score(
            new[] { Question(new[] { A }) },
            new Dictionary<Guid, IReadOnlyList<Guid>>());

        var answer = result.Single();
        Assert.False(answer.IsCorrect);
        Assert.Empty(answer.SelectedOptionIds);
    }

    [Fact]
    public void InvalidSelectedOption_IsIgnored()
    {
        var unknown = Guid.NewGuid();
        var result = AttemptScorer.Score(new[] { Question(new[] { A }) }, Submit(A, unknown));

        var answer = result.Single();
        Assert.True(answer.IsCorrect);
        Assert.DoesNotContain(unknown, answer.SelectedOptionIds);
    }
}
