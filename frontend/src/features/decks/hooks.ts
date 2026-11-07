import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
  createDeck,
  createFlashcard,
  deleteDeck,
  deleteFlashcard,
  getDeck,
  listDecks,
  listFlashcards,
  updateDeck,
  updateFlashcard,
  type ListDecksParams,
} from "./api";
import type { DeckInput, FlashcardInput } from "./types";

interface SaveFlashcardArgs {
  id?: string;
  input: FlashcardInput;
}

export const deckKeys = {
  all: ["decks"] as const,
  list: (params: ListDecksParams) => ["decks", "list", params] as const,
  detail: (id: string) => ["decks", "detail", id] as const,
  flashcards: (deckId: string) => ["decks", deckId, "flashcards"] as const,
};

export function useDecks(params: ListDecksParams) {
  return useQuery({
    queryKey: deckKeys.list(params),
    queryFn: () => listDecks(params),
    placeholderData: (previous) => previous,
  });
}

export function useDeck(id: string) {
  return useQuery({
    queryKey: deckKeys.detail(id),
    queryFn: () => getDeck(id),
  });
}

export function useCreateDeck() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: DeckInput) => createDeck(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: deckKeys.all }),
  });
}

export function useUpdateDeck(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: DeckInput) => updateDeck(id, input),
    onSuccess: (deck) => {
      queryClient.setQueryData(deckKeys.detail(id), deck);
      queryClient.invalidateQueries({ queryKey: deckKeys.all });
    },
  });
}

export function useDeleteDeck() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteDeck(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: deckKeys.all }),
  });
}

export function useFlashcards(deckId: string) {
  return useQuery({
    queryKey: deckKeys.flashcards(deckId),
    queryFn: () => listFlashcards(deckId),
  });
}

export function useSaveFlashcard(deckId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: SaveFlashcardArgs) =>
      id ? updateFlashcard(deckId, id, input) : createFlashcard(deckId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: deckKeys.flashcards(deckId) });
      queryClient.invalidateQueries({ queryKey: deckKeys.detail(deckId) });
      queryClient.invalidateQueries({ queryKey: deckKeys.all });
    },
  });
}

export function useDeleteFlashcard(deckId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteFlashcard(deckId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: deckKeys.flashcards(deckId) });
      queryClient.invalidateQueries({ queryKey: deckKeys.detail(deckId) });
      queryClient.invalidateQueries({ queryKey: deckKeys.all });
    },
  });
}
