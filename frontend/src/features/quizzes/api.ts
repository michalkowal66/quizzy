import { api } from "@/lib/api";
import type { PagedResult } from "@/lib/pagination";
import type {
  AnswerInput,
  AttemptResult,
  AttemptSummary,
  Question,
  QuestionInput,
  Quiz,
  QuizInput,
} from "./types";

export interface ListQuizzesParams {
  page?: number;
  pageSize?: number;
  search?: string;
}

export async function listQuizzes(
  params: ListQuizzesParams,
): Promise<PagedResult<Quiz>> {
  const { data } = await api.get<PagedResult<Quiz>>("/quizzes", { params });
  return data;
}

export async function getQuiz(id: string): Promise<Quiz> {
  const { data } = await api.get<Quiz>(`/quizzes/${id}`);
  return data;
}

export async function createQuiz(input: QuizInput): Promise<Quiz> {
  const { data } = await api.post<Quiz>("/quizzes", input);
  return data;
}

export async function updateQuiz(id: string, input: QuizInput): Promise<Quiz> {
  const { data } = await api.put<Quiz>(`/quizzes/${id}`, input);
  return data;
}

export async function deleteQuiz(id: string): Promise<void> {
  await api.delete(`/quizzes/${id}`);
}

export async function listQuestions(quizId: string): Promise<Question[]> {
  const { data } = await api.get<Question[]>(`/quizzes/${quizId}/questions`);
  return data;
}

export async function createQuestion(
  quizId: string,
  input: QuestionInput,
): Promise<Question> {
  const { data } = await api.post<Question>(
    `/quizzes/${quizId}/questions`,
    input,
  );
  return data;
}

export async function updateQuestion(
  quizId: string,
  id: string,
  input: QuestionInput,
): Promise<Question> {
  const { data } = await api.put<Question>(
    `/quizzes/${quizId}/questions/${id}`,
    input,
  );
  return data;
}

export async function deleteQuestion(quizId: string, id: string): Promise<void> {
  await api.delete(`/quizzes/${quizId}/questions/${id}`);
}

export async function submitAttempt(
  quizId: string,
  answers: AnswerInput[],
): Promise<AttemptResult> {
  const { data } = await api.post<AttemptResult>(
    `/quizzes/${quizId}/attempts`,
    { quizId, answers },
  );
  return data;
}

export async function listAttempts(quizId: string): Promise<AttemptSummary[]> {
  const { data } = await api.get<AttemptSummary[]>(
    `/quizzes/${quizId}/attempts`,
  );
  return data;
}
