import { useState } from "react";
import { Link, useParams } from "react-router-dom";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/problem";
import { useQuestions, useQuiz, useSubmitAttempt } from "./hooks";
import type { AnswerInput, AttemptResult, Question } from "./types";

type Selections = Record<string, string[]>;

function formatPercentage(value: number): string {
  return `${Math.round(value)}%`;
}

export function SolvePage() {
  const { quizId = "" } = useParams();
  const quizQuery = useQuiz(quizId);
  const questionsQuery = useQuestions(quizId);
  const submitAttempt = useSubmitAttempt(quizId);

  const [selections, setSelections] = useState<Selections>({});
  const [result, setResult] = useState<AttemptResult | null>(null);
  const [step, setStep] = useState(0);

  const toggleOption = (question: Question, optionId: string) => {
    setSelections((current) => {
      const previous = current[question.id] ?? [];
      if (question.type === "MultipleChoice") {
        const next = previous.includes(optionId)
          ? previous.filter((id) => id !== optionId)
          : [...previous, optionId];
        return { ...current, [question.id]: next };
      }
      return { ...current, [question.id]: [optionId] };
    });
  };

  if (quizQuery.isPending || questionsQuery.isPending) {
    return <p className="text-muted-foreground">Loading…</p>;
  }

  if (questionsQuery.isError) {
    return (
      <p className="text-destructive">
        {getErrorMessage(questionsQuery.error)}
      </p>
    );
  }

  const quizTitle = quizQuery.data?.title ?? "Quiz";
  const questions = questionsQuery.data ?? [];

  if (questions.length === 0) {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-muted-foreground">
          This quiz has no questions to solve.
        </p>
        <Link
          to={`/quizzes/${quizId}`}
          className="text-sm underline underline-offset-4"
        >
          Back to quiz
        </Link>
      </div>
    );
  }

  const answeredCount = questions.filter(
    (question) => (selections[question.id] ?? []).length > 0,
  ).length;

  const handleSubmit = () => {
    const answers: AnswerInput[] = questions.map((question) => ({
      questionId: question.id,
      selectedOptionIds: selections[question.id] ?? [],
    }));
    submitAttempt.mutate(answers, { onSuccess: setResult });
  };

  const restart = () => {
    setSelections({});
    setResult(null);
    setStep(0);
  };

  if (result) {
    const resultById = new Map(
      result.answers.map((answer) => [answer.questionId, answer]),
    );
    return (
      <div className="mx-auto flex max-w-2xl flex-col gap-6">
        <Card>
          <CardHeader className="items-center text-center">
            <CardTitle className="text-xl">
              {result.score} / {result.maxScore}
            </CardTitle>
            <p className="text-muted-foreground">
              You scored {formatPercentage(result.percentage)} on {quizTitle}.
            </p>
          </CardHeader>
        </Card>

        <div className="flex flex-col gap-3">
          {questions.map((question, index) => {
            const answer = resultById.get(question.id);
            const selected = answer?.selectedOptionIds ?? [];
            const correct = answer?.correctOptionIds ?? [];
            return (
              <Card key={question.id}>
                <CardContent className="flex flex-col gap-2 p-4">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-sm font-medium">
                      {index + 1}. {question.text}
                    </p>
                    <span
                      className={
                        answer?.isCorrect
                          ? "text-sm font-medium text-green-600 dark:text-green-500"
                          : "text-sm font-medium text-destructive"
                      }
                    >
                      {answer?.isCorrect ? "Correct" : "Incorrect"}
                    </span>
                  </div>
                  <ul className="flex flex-col gap-1">
                    {question.options.map((option) => {
                      const isCorrect = correct.includes(option.id);
                      const isSelected = selected.includes(option.id);
                      return (
                        <li
                          key={option.id}
                          className={
                            isCorrect
                              ? "text-sm text-green-600 dark:text-green-500"
                              : isSelected
                                ? "text-sm text-destructive"
                                : "text-sm text-muted-foreground"
                          }
                        >
                          {isCorrect ? "✓" : isSelected ? "✗" : "•"}{" "}
                          {option.text}
                        </li>
                      );
                    })}
                  </ul>
                </CardContent>
              </Card>
            );
          })}
        </div>

        <div className="flex justify-center gap-2">
          <Button onClick={restart}>Try again</Button>
          <Link
            to={`/quizzes/${quizId}`}
            className={buttonVariants({ variant: "outline" })}
          >
            Back to quiz
          </Link>
        </div>
      </div>
    );
  }

  const current = questions[step];
  const selectedForCurrent = selections[current.id] ?? [];
  const isLastStep = step === questions.length - 1;

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="flex items-center justify-between">
        <Link
          to={`/quizzes/${quizId}`}
          className="text-sm text-muted-foreground underline underline-offset-4"
        >
          ← {quizTitle}
        </Link>
        <span className="text-sm text-muted-foreground">
          {answeredCount} / {questions.length} answered
        </span>
      </div>

      <div className="flex flex-col gap-2">
        <span className="text-sm text-muted-foreground">
          Question {step + 1} of {questions.length}
        </span>
        <div
          className="h-1.5 w-full overflow-hidden rounded-full bg-secondary"
          role="progressbar"
          aria-valuenow={step + 1}
          aria-valuemin={1}
          aria-valuemax={questions.length}
        >
          <div
            className="h-full rounded-full bg-primary transition-all"
            style={{ width: `${((step + 1) / questions.length) * 100}%` }}
          />
        </div>
      </div>

      <Card>
        <CardContent className="flex flex-col gap-3 p-4">
          <p className="text-sm font-medium">
            {step + 1}. {current.text}
          </p>
          <div className="flex flex-col gap-2">
            {current.options.map((option) => (
              <label
                key={option.id}
                className="flex cursor-pointer items-center gap-2 text-sm"
              >
                <input
                  type={
                    current.type === "MultipleChoice" ? "checkbox" : "radio"
                  }
                  name={`question-${current.id}`}
                  checked={selectedForCurrent.includes(option.id)}
                  onChange={() => toggleOption(current, option.id)}
                  className="size-4 shrink-0 accent-primary"
                />
                {option.text}
              </label>
            ))}
          </div>
        </CardContent>
      </Card>

      {submitAttempt.isError && (
        <p className="text-sm text-destructive">
          {getErrorMessage(submitAttempt.error)}
        </p>
      )}

      <div className="flex items-center justify-between gap-2">
        <Button
          variant="outline"
          onClick={() => setStep((previous) => previous - 1)}
          disabled={step === 0}
        >
          Previous
        </Button>
        {isLastStep ? (
          <Button
            onClick={handleSubmit}
            disabled={submitAttempt.isPending || answeredCount === 0}
          >
            {submitAttempt.isPending ? "Submitting…" : "Submit"}
          </Button>
        ) : (
          <Button onClick={() => setStep((previous) => previous + 1)}>
            Next
          </Button>
        )}
      </div>

      <p className="text-center text-xs text-muted-foreground">
        {answeredCount < questions.length
          ? "Unanswered questions count as incorrect."
          : "All questions answered."}
      </p>
    </div>
  );
}
