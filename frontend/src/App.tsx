import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import { AppLayout } from "@/components/layout/AppLayout";
import { AdminUserDetailPage } from "@/features/admin/AdminUserDetailPage";
import { AdminUsersPage } from "@/features/admin/AdminUsersPage";
import { AuthProvider } from "@/features/auth/AuthProvider";
import { LoginPage } from "@/features/auth/LoginPage";
import { RegisterPage } from "@/features/auth/RegisterPage";
import { DashboardPage } from "@/features/dashboard/DashboardPage";
import { DeckDetailPage } from "@/features/decks/DeckDetailPage";
import { DecksPage } from "@/features/decks/DecksPage";
import { FoldersPage } from "@/features/folders/FoldersPage";
import { QuizDetailPage } from "@/features/quizzes/QuizDetailPage";
import { QuizzesPage } from "@/features/quizzes/QuizzesPage";
import { SolvePage } from "@/features/quizzes/SolvePage";
import { StudyPage } from "@/features/study/StudyPage";
import { AdminRoute } from "@/routes/AdminRoute";
import { ProtectedRoute } from "@/routes/ProtectedRoute";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="folders" element={<FoldersPage />} />
                <Route path="decks" element={<DecksPage />} />
                <Route path="decks/:deckId" element={<DeckDetailPage />} />
                <Route path="decks/:deckId/study" element={<StudyPage />} />
                <Route path="quizzes" element={<QuizzesPage />} />
                <Route path="quizzes/:quizId" element={<QuizDetailPage />} />
                <Route
                  path="quizzes/:quizId/solve"
                  element={<SolvePage />}
                />
                <Route element={<AdminRoute />}>
                  <Route path="admin/users" element={<AdminUsersPage />} />
                  <Route
                    path="admin/users/:userId"
                    element={<AdminUserDetailPage />}
                  />
                </Route>
              </Route>
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
