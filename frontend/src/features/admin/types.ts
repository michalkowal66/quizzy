export type UserRole = "User" | "Admin";

export interface UserSummary {
  id: string;
  email: string;
  displayName: string;
  role: UserRole;
  createdAt: string;
}

export interface UserDetail extends UserSummary {
  currentStreak: number;
  longestStreak: number;
  lastActivity: string | null;
  deckCount: number;
  flashcardCount: number;
  quizCount: number;
  attemptCount: number;
}
