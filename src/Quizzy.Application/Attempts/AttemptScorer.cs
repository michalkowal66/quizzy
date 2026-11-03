namespace Quizzy.Application.Attempts;

/// <summary>A question reduced to what is needed to score an answer.</summary>
public record ScorableQuestion(
    Guid QuestionId,
    IReadOnlyList<Guid> OptionIds,
    IReadOnlyList<Guid> CorrectOptionIds);

/// <summary>The outcome of scoring a single question.</summary>
public record ScoredAnswer(
    Guid QuestionId,
    IReadOnlyList<Guid> SelectedOptionIds,
    IReadOnlyList<Guid> CorrectOptionIds,
    bool IsCorrect);

/// <summary>
/// Pure scoring logic for a quiz attempt, independent of persistence.
/// </summary>
public static class AttemptScorer
{
    public static IReadOnlyList<ScoredAnswer> Score(
        IEnumerable<ScorableQuestion> questions,
        IReadOnlyDictionary<Guid, IReadOnlyList<Guid>> submittedByQuestion)
    {
        var results = new List<ScoredAnswer>();

        foreach (var question in questions)
        {
            var validOptions = question.OptionIds.ToHashSet();

            var selected = submittedByQuestion.TryGetValue(question.QuestionId, out var raw)
                ? raw.Where(validOptions.Contains).ToHashSet()
                : new HashSet<Guid>();

            var correct = question.CorrectOptionIds.ToHashSet();
            var isCorrect = selected.SetEquals(correct);

            results.Add(new ScoredAnswer(
                question.QuestionId,
                selected.ToList(),
                correct.ToList(),
                isCorrect));
        }

        return results;
    }
}
