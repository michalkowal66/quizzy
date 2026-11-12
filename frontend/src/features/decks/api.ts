import { api } from "@/lib/api";
import type { PagedResult } from "@/lib/pagination";
import type { Deck, DeckInput, Flashcard, FlashcardInput } from "./types";

export interface ListDecksParams {
  page?: number;
  pageSize?: number;
  search?: string;
  folderId?: string;
}

export async function listDecks(
  params: ListDecksParams,
): Promise<PagedResult<Deck>> {
  const { data } = await api.get<PagedResult<Deck>>("/decks", { params });
  return data;
}

export async function getDeck(id: string): Promise<Deck> {
  const { data } = await api.get<Deck>(`/decks/${id}`);
  return data;
}

export async function createDeck(input: DeckInput): Promise<Deck> {
  const { data } = await api.post<Deck>("/decks", input);
  return data;
}

export async function updateDeck(id: string, input: DeckInput): Promise<Deck> {
  const { data } = await api.put<Deck>(`/decks/${id}`, input);
  return data;
}

export async function deleteDeck(id: string): Promise<void> {
  await api.delete(`/decks/${id}`);
}

export async function listFlashcards(deckId: string): Promise<Flashcard[]> {
  const { data } = await api.get<Flashcard[]>(`/decks/${deckId}/flashcards`);
  return data;
}

export async function createFlashcard(
  deckId: string,
  input: FlashcardInput,
): Promise<Flashcard> {
  const { data } = await api.post<Flashcard>(
    `/decks/${deckId}/flashcards`,
    input,
  );
  return data;
}

export async function updateFlashcard(
  deckId: string,
  id: string,
  input: FlashcardInput,
): Promise<Flashcard> {
  const { data } = await api.put<Flashcard>(
    `/decks/${deckId}/flashcards/${id}`,
    input,
  );
  return data;
}

export async function deleteFlashcard(
  deckId: string,
  id: string,
): Promise<void> {
  await api.delete(`/decks/${deckId}/flashcards/${id}`);
}
