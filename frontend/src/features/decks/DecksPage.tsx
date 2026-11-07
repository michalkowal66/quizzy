import { useState } from "react";
import { Link } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/problem";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import { DeckFormDialog, type DeckFormValues } from "./DeckFormDialog";
import { useCreateDeck, useDecks } from "./hooks";

const PAGE_SIZE = 12;

export function DecksPage() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [createOpen, setCreateOpen] = useState(false);

  const debouncedSearch = useDebouncedValue(search);
  const decksQuery = useDecks({
    page,
    pageSize: PAGE_SIZE,
    search: debouncedSearch || undefined,
  });
  const createDeck = useCreateDeck();

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(1);
  };

  const handleCreate = (values: DeckFormValues) => {
    createDeck.mutate(values, { onSuccess: () => setCreateOpen(false) });
  };

  const result = decksQuery.data;
  const totalPages = result?.totalPages ?? 0;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">Decks</h1>
          <p className="text-muted-foreground">
            Build and review your flashcard decks.
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>New deck</Button>
      </div>

      <Input
        placeholder="Search decks…"
        value={search}
        onChange={(event) => handleSearchChange(event.target.value)}
        className="max-w-sm"
      />

      {decksQuery.isPending ? (
        <p className="text-muted-foreground">Loading decks…</p>
      ) : decksQuery.isError ? (
        <p className="text-destructive">{getErrorMessage(decksQuery.error)}</p>
      ) : result && result.items.length === 0 ? (
        <p className="text-muted-foreground">
          {debouncedSearch
            ? "No decks match your search."
            : "No decks yet. Create your first one."}
        </p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {result?.items.map((deck) => (
            <Link key={deck.id} to={`/decks/${deck.id}`} className="block">
              <Card className="h-full transition-colors hover:border-foreground/30">
                <CardHeader>
                  <CardTitle className="truncate">{deck.title}</CardTitle>
                </CardHeader>
                <CardContent className="flex flex-col gap-2">
                  <p className="line-clamp-2 min-h-10 text-sm text-muted-foreground">
                    {deck.description || "No description."}
                  </p>
                  <span className="text-xs text-muted-foreground">
                    {deck.flashcardCount}{" "}
                    {deck.flashcardCount === 1 ? "card" : "cards"}
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
        <DeckFormDialog
          open={createOpen}
          onOpenChange={setCreateOpen}
          title="New deck"
          submitLabel="Create"
          defaultValues={{ title: "", description: "" }}
          pending={createDeck.isPending}
          errorMessage={
            createDeck.isError ? getErrorMessage(createDeck.error) : null
          }
          onSubmit={handleCreate}
        />
      )}
    </div>
  );
}
