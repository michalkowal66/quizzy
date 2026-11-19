import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import { Button, buttonVariants } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { getErrorMessage } from "@/lib/problem";
import { useFolders } from "@/features/folders/hooks";
import { DeckFormDialog, type DeckFormValues } from "./DeckFormDialog";
import {
  FlashcardFormDialog,
  type FlashcardFormValues,
} from "./FlashcardFormDialog";
import {
  useDeck,
  useDeleteDeck,
  useDeleteFlashcard,
  useFlashcards,
  useSaveFlashcard,
  useUpdateDeck,
} from "./hooks";
import type { Flashcard } from "./types";

const PAGE_SIZE = 10;

export function DeckDetailPage() {
  const { deckId = "" } = useParams();
  const navigate = useNavigate();

  const deckQuery = useDeck(deckId);
  const foldersQuery = useFolders();
  const flashcardsQuery = useFlashcards(deckId);
  const updateDeck = useUpdateDeck(deckId);
  const deleteDeck = useDeleteDeck();
  const saveFlashcard = useSaveFlashcard(deckId);
  const deleteFlashcard = useDeleteFlashcard(deckId);

  const [editDeckOpen, setEditDeckOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<Flashcard | null>(null);
  const [cardDialogOpen, setCardDialogOpen] = useState(false);
  const [deleteDeckOpen, setDeleteDeckOpen] = useState(false);
  const [pendingCard, setPendingCard] = useState<Flashcard | null>(null);
  const [page, setPage] = useState(1);

  const deck = deckQuery.data;

  const handleUpdateDeck = (values: DeckFormValues) => {
    updateDeck.mutate(
      { ...values, folderId: values.folderId || null },
      { onSuccess: () => setEditDeckOpen(false) },
    );
  };

  const openDeleteDeck = () => {
    deleteDeck.reset();
    setDeleteDeckOpen(true);
  };

  const confirmDeleteDeck = () => {
    deleteDeck.mutate(deckId, { onSuccess: () => navigate("/decks") });
  };

  const openCreateCard = () => {
    setEditingCard(null);
    setCardDialogOpen(true);
  };

  const openEditCard = (card: Flashcard) => {
    setEditingCard(card);
    setCardDialogOpen(true);
  };

  const handleSaveCard = (values: FlashcardFormValues) => {
    saveFlashcard.mutate(
      { id: editingCard?.id, input: values },
      { onSuccess: () => setCardDialogOpen(false) },
    );
  };

  const openDeleteCard = (card: Flashcard) => {
    deleteFlashcard.reset();
    setPendingCard(card);
  };

  const confirmDeleteCard = () => {
    if (!pendingCard) {
      return;
    }
    deleteFlashcard.mutate(pendingCard.id, {
      onSuccess: () => setPendingCard(null),
    });
  };

  if (deckQuery.isPending) {
    return <p className="text-muted-foreground">Loading deck…</p>;
  }

  if (deckQuery.isError || !deck) {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-destructive">{getErrorMessage(deckQuery.error)}</p>
        <Link to="/decks" className="text-sm underline underline-offset-4">
          Back to decks
        </Link>
      </div>
    );
  }

  const flashcards = flashcardsQuery.data ?? [];
  const folderName = deck.folderId
    ? (foldersQuery.data ?? []).find((folder) => folder.id === deck.folderId)
        ?.name
    : undefined;

  const totalPages = Math.max(1, Math.ceil(flashcards.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const visibleFlashcards = flashcards.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE,
  );

  return (
    <div className="flex flex-col gap-6">
      <Link
        to="/decks"
        className="text-sm text-muted-foreground underline underline-offset-4"
      >
        ← Back to decks
      </Link>

      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">{deck.title}</h1>
          {deck.description && (
            <p className="text-muted-foreground">{deck.description}</p>
          )}
          {folderName && (
            <p className="mt-1 text-xs text-muted-foreground">
              In {folderName}
            </p>
          )}
        </div>
        <div className="flex items-center gap-3">
          {flashcards.length > 0 && (
            <Link to={`/decks/${deckId}/study`} className={buttonVariants()}>
              Study
            </Link>
          )}
          <div className="flex gap-1">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setEditDeckOpen(true)}
            >
              Edit
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="text-destructive hover:bg-destructive/10 hover:text-destructive"
              onClick={openDeleteDeck}
              disabled={deleteDeck.isPending}
            >
              Delete
            </Button>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium">Flashcards</h2>
        <Button size="sm" onClick={openCreateCard}>
          Add flashcard
        </Button>
      </div>

      {flashcardsQuery.isPending ? (
        <p className="text-muted-foreground">Loading flashcards…</p>
      ) : flashcardsQuery.isError ? (
        <p className="text-destructive">
          {getErrorMessage(flashcardsQuery.error)}
        </p>
      ) : flashcards.length === 0 ? (
        <p className="text-muted-foreground">
          No flashcards yet. Add your first one.
        </p>
      ) : (
        <>
          <div className="flex flex-col gap-3">
            {visibleFlashcards.map((card) => (
              <Card key={card.id}>
                <CardContent className="flex items-center justify-between gap-4 p-4">
                  <div className="grid flex-1 gap-1 sm:grid-cols-2">
                    <p className="text-sm">{card.front}</p>
                    <p className="text-sm text-muted-foreground">{card.back}</p>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => openEditCard(card)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => openDeleteCard(card)}
                    >
                      Delete
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4">
              <Button
                variant="outline"
                size="sm"
                disabled={currentPage <= 1}
                onClick={() => setPage(currentPage - 1)}
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {currentPage} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={currentPage >= totalPages}
                onClick={() => setPage(currentPage + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}

      {editDeckOpen && (
        <DeckFormDialog
          open={editDeckOpen}
          onOpenChange={setEditDeckOpen}
          title="Edit deck"
          submitLabel="Save"
          defaultValues={{
            title: deck.title,
            description: deck.description ?? "",
            folderId: deck.folderId ?? "",
          }}
          pending={updateDeck.isPending}
          errorMessage={
            updateDeck.isError ? getErrorMessage(updateDeck.error) : null
          }
          onSubmit={handleUpdateDeck}
        />
      )}

      {cardDialogOpen && (
        <FlashcardFormDialog
          open={cardDialogOpen}
          onOpenChange={setCardDialogOpen}
          title={editingCard ? "Edit flashcard" : "Add flashcard"}
          submitLabel={editingCard ? "Save" : "Add"}
          defaultValues={{
            front: editingCard?.front ?? "",
            back: editingCard?.back ?? "",
          }}
          pending={saveFlashcard.isPending}
          errorMessage={
            saveFlashcard.isError ? getErrorMessage(saveFlashcard.error) : null
          }
          onSubmit={handleSaveCard}
        />
      )}

      <ConfirmDialog
        open={deleteDeckOpen}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteDeckOpen(false);
          }
        }}
        title="Delete deck"
        description="Delete this deck and all of its flashcards? This cannot be undone."
        confirmLabel="Delete"
        destructive
        pending={deleteDeck.isPending}
        errorMessage={deleteDeck.isError ? getErrorMessage(deleteDeck.error) : null}
        onConfirm={confirmDeleteDeck}
      />

      <ConfirmDialog
        open={pendingCard !== null}
        onOpenChange={(open) => {
          if (!open) {
            setPendingCard(null);
          }
        }}
        title="Delete flashcard"
        description="Delete this flashcard? This cannot be undone."
        confirmLabel="Delete"
        destructive
        pending={deleteFlashcard.isPending}
        errorMessage={
          deleteFlashcard.isError ? getErrorMessage(deleteFlashcard.error) : null
        }
        onConfirm={confirmDeleteCard}
      />
    </div>
  );
}
