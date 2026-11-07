export interface Deck {
  id: string;
  title: string;
  description: string | null;
  folderId: string | null;
  flashcardCount: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface Flashcard {
  id: string;
  deckId: string;
  front: string;
  back: string;
  createdAt: string;
}

export interface DeckInput {
  title: string;
  description?: string | null;
  folderId?: string | null;
}

export interface FlashcardInput {
  front: string;
  back: string;
}
