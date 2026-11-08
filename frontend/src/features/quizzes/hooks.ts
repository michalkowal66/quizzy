import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
  createQuestion,
  createQuiz,
  deleteQuestion,
  deleteQuiz,
  getQuiz,
  listAttempts,
  listQuestions,
  listQuizzes,
  submitAttempt,
  updateQuestion,
  updateQuiz,
  type ListQuizzesParams,
} from "./api";
import type { AnswerInput, QuestionInput, QuizInput } from "./types";

interface SaveQuestionArgs {
  id?: string;
  input: QuestionInput;
}

export const quizKeys = {
  all: ["quizzes"] as const,
  list: (params: ListQuizzesParams) => ["quizzes", "list", params] as const,
  detail: (id: string) => ["quizzes", "detail", id] as const,
  questions: (quizId: string) => ["quizzes", quizId, "questions"] as const,
  attempts: (quizId: string) => ["quizzes", quizId, "attempts"] as const,
};

export function useQuizzes(params: ListQuizzesParams) {
  return useQuery({
    queryKey: quizKeys.list(params),
    queryFn: () => listQuizzes(params),
    placeholderData: (previous) => previous,
  });
}

export function useQuiz(id: string) {
  return useQuery({
    queryKey: quizKeys.detail(id),
    queryFn: () => getQuiz(id),
  });
}

export function useCreateQuiz() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: QuizInput) => createQuiz(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: quizKeys.all }),
  });
}

export function useUpdateQuiz(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: QuizInput) => updateQuiz(id, input),
    onSuccess: (quiz) => {
      queryClient.setQueryData(quizKeys.detail(id), quiz);
      queryClient.invalidateQueries({ queryKey: quizKeys.all });
    },
  });
}

export function useDeleteQuiz() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteQuiz(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: quizKeys.all }),
  });
}

export function useQuestions(quizId: string) {
  return useQuery({
    queryKey: quizKeys.questions(quizId),
    queryFn: () => listQuestions(quizId),
  });
}

export function useSaveQuestion(quizId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: SaveQuestionArgs) =>
      id
        ? updateQuestion(quizId, id, input)
        : createQuestion(quizId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: quizKeys.questions(quizId) });
      queryClient.invalidateQueries({ queryKey: quizKeys.detail(quizId) });
      queryClient.invalidateQueries({ queryKey: quizKeys.all });
    },
  });
}

export function useDeleteQuestion(quizId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteQuestion(quizId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: quizKeys.questions(quizId) });
      queryClient.invalidateQueries({ queryKey: quizKeys.detail(quizId) });
      queryClient.invalidateQueries({ queryKey: quizKeys.all });
    },
  });
}

export function useAttempts(quizId: string) {
  return useQuery({
    queryKey: quizKeys.attempts(quizId),
    queryFn: () => listAttempts(quizId),
  });
}

export function useSubmitAttempt(quizId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (answers: AnswerInput[]) => submitAttempt(quizId, answers),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: quizKeys.attempts(quizId) }),
  });
}
