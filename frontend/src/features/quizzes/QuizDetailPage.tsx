import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/problem";
import { useFolders } from "@/features/folders/hooks";
import { QuestionFormDialog } from "./QuestionFormDialog";
import { QuizFormDialog, type QuizFormValues } from "./QuizFormDialog";
import {
  useAttempts,
  useDeleteQuestion,
  useDeleteQuiz,
  useQuestions,
  useQuiz,
  useSaveQuestion,
  useUpdateQuiz,
} from "./hooks";
import {
  questionTypeLabels,
  type Question,
  type QuestionInput,
} from "./types";

const emptyQuestion: QuestionInput = {
  text: "",
  type: "SingleChoice",
  options: [
    { text: "", isCorrect: false },
    { text: "", isCorrect: false },
  ],
};

function formatPercentage(value: number): string {
  return `${Math.round(value)}%`;
}

export function QuizDetailPage() {
  const { quizId = "" } = useParams();
  const navigate = useNavigate();

  const quizQuery = useQuiz(quizId);
  const foldersQuery = useFolders();
  const questionsQuery = useQuestions(quizId);
  const attemptsQuery = useAttempts(quizId);
  const updateQuiz = useUpdateQuiz(quizId);
  const deleteQuiz = useDeleteQuiz();
  const saveQuestion = useSaveQuestion(quizId);
  const deleteQuestion = useDeleteQuestion(quizId);

  const [editQuizOpen, setEditQuizOpen] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<Question | null>(null);
  const [questionDialogOpen, setQuestionDialogOpen] = useState(false);

  const quiz = quizQuery.data;

  const handleUpdateQuiz = (values: QuizFormValues) => {
    updateQuiz.mutate(
      { ...values, folderId: values.folderId || null },
      { onSuccess: () => setEditQuizOpen(false) },
    );
  };

  const handleDeleteQuiz = () => {
    if (!window.confirm("Delete this quiz and all of its questions?")) {
      return;
    }
    deleteQuiz.mutate(quizId, { onSuccess: () => navigate("/quizzes") });
  };

  const openCreateQuestion = () => {
    setEditingQuestion(null);
    setQuestionDialogOpen(true);
  };

  const openEditQuestion = (question: Question) => {
    setEditingQuestion(question);
    setQuestionDialogOpen(true);
  };

  const handleSaveQuestion = (values: QuestionInput) => {
    saveQuestion.mutate(
      { id: editingQuestion?.id, input: values },
      { onSuccess: () => setQuestionDialogOpen(false) },
    );
  };

  const handleDeleteQuestion = (question: Question) => {
    if (window.confirm("Delete this question?")) {
      deleteQuestion.mutate(question.id);
    }
  };

  if (quizQuery.isPending) {
    return <p className="text-muted-foreground">Loading quiz…</p>;
  }

  if (quizQuery.isError || !quiz) {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-destructive">{getErrorMessage(quizQuery.error)}</p>
        <Link to="/quizzes" className="text-sm underline underline-offset-4">
          Back to quizzes
        </Link>
      </div>
    );
  }

  const questions = questionsQuery.data ?? [];
  const attempts = attemptsQuery.data ?? [];
  const folderName = quiz.folderId
    ? (foldersQuery.data ?? []).find((folder) => folder.id === quiz.folderId)
        ?.name
    : undefined;

  return (
    <div className="flex flex-col gap-6">
      <Link
        to="/quizzes"
        className="text-sm text-muted-foreground underline underline-offset-4"
      >
        ← Back to quizzes
      </Link>

      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">{quiz.title}</h1>
          {quiz.description && (
            <p className="text-muted-foreground">{quiz.description}</p>
          )}
          {folderName && (
            <p className="mt-1 text-xs text-muted-foreground">In {folderName}</p>
          )}
        </div>
        <div className="flex items-center gap-3">
          {questions.length > 0 && (
            <Link
              to={`/quizzes/${quizId}/solve`}
              className={buttonVariants()}
            >
              Solve
            </Link>
          )}
          <div className="flex gap-1">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setEditQuizOpen(true)}
            >
              Edit
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="text-destructive hover:bg-destructive/10 hover:text-destructive"
              onClick={handleDeleteQuiz}
              disabled={deleteQuiz.isPending}
            >
              Delete
            </Button>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium">Questions</h2>
        <Button size="sm" onClick={openCreateQuestion}>
          Add question
        </Button>
      </div>

      {questionsQuery.isPending ? (
        <p className="text-muted-foreground">Loading questions…</p>
      ) : questionsQuery.isError ? (
        <p className="text-destructive">
          {getErrorMessage(questionsQuery.error)}
        </p>
      ) : questions.length === 0 ? (
        <p className="text-muted-foreground">
          No questions yet. Add your first one.
        </p>
      ) : (
        <div className="flex flex-col gap-3">
          {questions.map((question, index) => (
            <Card key={question.id}>
              <CardContent className="flex items-start justify-between gap-4 p-4">
                <div className="flex flex-col gap-2">
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-muted-foreground">
                      {index + 1}.
                    </span>
                    <span className="rounded bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
                      {questionTypeLabels[question.type]}
                    </span>
                  </div>
                  <p className="text-sm font-medium">{question.text}</p>
                  <ul className="flex flex-col gap-1">
                    {question.options.map((option) => (
                      <li
                        key={option.id}
                        className={
                          option.isCorrect
                            ? "text-sm text-foreground"
                            : "text-sm text-muted-foreground"
                        }
                      >
                        {option.isCorrect ? "✓" : "•"} {option.text}
                      </li>
                    ))}
                  </ul>
                </div>
                <div className="flex shrink-0 gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => openEditQuestion(question)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDeleteQuestion(question)}
                  >
                    Delete
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {attempts.length > 0 && (
        <div className="flex flex-col gap-3">
          <h2 className="text-lg font-medium">Past attempts</h2>
          <Card>
            <CardContent className="flex flex-col divide-y p-0">
              {attempts.map((attempt) => (
                <div
                  key={attempt.id}
                  className="flex items-center justify-between px-4 py-3 text-sm"
                >
                  <span className="text-muted-foreground">
                    {new Date(attempt.completedAt).toLocaleString()}
                  </span>
                  <span>
                    {attempt.score} / {attempt.maxScore} (
                    {formatPercentage(attempt.percentage)})
                  </span>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      )}

      {editQuizOpen && (
        <QuizFormDialog
          open={editQuizOpen}
          onOpenChange={setEditQuizOpen}
          title="Edit quiz"
          submitLabel="Save"
          defaultValues={{
            title: quiz.title,
            description: quiz.description ?? "",
            folderId: quiz.folderId ?? "",
          }}
          pending={updateQuiz.isPending}
          errorMessage={
            updateQuiz.isError ? getErrorMessage(updateQuiz.error) : null
          }
          onSubmit={handleUpdateQuiz}
        />
      )}

      {questionDialogOpen && (
        <QuestionFormDialog
          open={questionDialogOpen}
          onOpenChange={setQuestionDialogOpen}
          title={editingQuestion ? "Edit question" : "Add question"}
          submitLabel={editingQuestion ? "Save" : "Add"}
          defaultValues={
            editingQuestion
              ? {
                  text: editingQuestion.text,
                  type: editingQuestion.type,
                  options: editingQuestion.options.map((option) => ({
                    text: option.text,
                    isCorrect: option.isCorrect,
                  })),
                }
              : emptyQuestion
          }
          pending={saveQuestion.isPending}
          errorMessage={
            saveQuestion.isError ? getErrorMessage(saveQuestion.error) : null
          }
          onSubmit={handleSaveQuestion}
        />
      )}
    </div>
  );
}
