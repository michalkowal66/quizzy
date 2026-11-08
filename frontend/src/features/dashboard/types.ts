export interface DashboardStats {
  currentStreak: number;
  longestStreak: number;
  studiedToday: boolean;
  lastActivity: string | null;
  deckCount: number;
  flashcardCount: number;
  quizCount: number;
  attemptCount: number;
}
