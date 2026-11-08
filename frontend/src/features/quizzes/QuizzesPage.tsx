import { useState } from "react";
import { Link } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/problem";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import { QuizFormDialog, type QuizFormValues } from "./QuizFormDialog";
import { useCreateQuiz, useQuizzes } from "./hooks";

const PAGE_SIZE = 12;

export function QuizzesPage() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [createOpen, setCreateOpen] = useState(false);

  const debouncedSearch = useDebouncedValue(search);
  const quizzesQuery = useQuizzes({
    page,
    pageSize: PAGE_SIZE,
    search: debouncedSearch || undefined,
  });
  const createQuiz = useCreateQuiz();

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(1);
  };

  const handleCreate = (values: QuizFormValues) => {
    createQuiz.mutate(values, { onSuccess: () => setCreateOpen(false) });
  };

  const result = quizzesQuery.data;
  const totalPages = result?.totalPages ?? 0;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">Quizzes</h1>
          <p className="text-muted-foreground">
            Create quizzes and test what you have learned.
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>New quiz</Button>
      </div>

      <Input
        placeholder="Search quizzes…"
        value={search}
        onChange={(event) => handleSearchChange(event.target.value)}
        className="max-w-sm"
      />

      {quizzesQuery.isPending ? (
        <p className="text-muted-foreground">Loading quizzes…</p>
      ) : quizzesQuery.isError ? (
        <p className="text-destructive">
          {getErrorMessage(quizzesQuery.error)}
        </p>
      ) : result && result.items.length === 0 ? (
        <p className="text-muted-foreground">
          {debouncedSearch
            ? "No quizzes match your search."
            : "No quizzes yet. Create your first one."}
        </p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {result?.items.map((quiz) => (
            <Link key={quiz.id} to={`/quizzes/${quiz.id}`} className="block">
              <Card className="h-full transition-colors hover:border-foreground/30">
                <CardHeader>
                  <CardTitle className="truncate">{quiz.title}</CardTitle>
                </CardHeader>
                <CardContent className="flex flex-col gap-2">
                  <p className="line-clamp-2 min-h-10 text-sm text-muted-foreground">
                    {quiz.description || "No description."}
                  </p>
                  <span className="text-xs text-muted-foreground">
                    {quiz.questionCount}{" "}
                    {quiz.questionCount === 1 ? "question" : "questions"}
                  </span>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-4">
          <Button
            variant="outline"
            size="sm"
            disabled={page <= 1}
            onClick={() => setPage((current) => current - 1)}
          >
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {page} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= totalPages}
            onClick={() => setPage((current) => current + 1)}
          >
            Next
          </Button>
        </div>
      )}

      {createOpen && (
        <QuizFormDialog
          open={createOpen}
          onOpenChange={setCreateOpen}
          title="New quiz"
          submitLabel="Create"
          defaultValues={{ title: "", description: "" }}
          pending={createQuiz.isPending}
          errorMessage={
            createQuiz.isError ? getErrorMessage(createQuiz.error) : null
          }
          onSubmit={handleCreate}
        />
      )}
    </div>
  );
}
