export interface Folder {
  id: string;
  name: string;
  deckCount: number;
  quizCount: number;
  createdAt: string;
}

export interface FolderInput {
  name: string;
}
