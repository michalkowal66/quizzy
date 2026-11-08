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

      <div className="flex flex-col gap-3">
        {questions.map((question, index) => {
          const selected = selections[question.id] ?? [];
          return (
            <Card key={question.id}>
              <CardContent className="flex flex-col gap-3 p-4">
                <p className="text-sm font-medium">
                  {index + 1}. {question.text}
                </p>
                <div className="flex flex-col gap-2">
                  {question.options.map((option) => (
                    <label
                      key={option.id}
                      className="flex cursor-pointer items-center gap-2 text-sm"
                    >
                      <input
                        type={
                          question.type === "MultipleChoice"
                            ? "checkbox"
                            : "radio"
                        }
                        name={`question-${question.id}`}
                        checked={selected.includes(option.id)}
                        onChange={() => toggleOption(question, option.id)}
                        className="size-4 shrink-0 accent-primary"
                      />
                      {option.text}
                    </label>
                  ))}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {submitAttempt.isError && (
        <p className="text-sm text-destructive">
          {getErrorMessage(submitAttempt.error)}
        </p>
      )}

      <div className="flex items-center justify-between gap-4">
        <span className="text-sm text-muted-foreground">
          {answeredCount < questions.length
            ? "Unanswered questions count as incorrect."
            : "All questions answered."}
        </span>
        <Button
          onClick={handleSubmit}
          disabled={submitAttempt.isPending || answeredCount === 0}
        >
          {submitAttempt.isPending ? "Submitting…" : "Submit"}
        </Button>
      </div>
    </div>
  );
}
