export type QuestionType = "SingleChoice" | "MultipleChoice" | "TrueFalse";

export interface Quiz {
  id: string;
  title: string;
  description: string | null;
  folderId: string | null;
  questionCount: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface AnswerOption {
  id: string;
  text: string;
  isCorrect: boolean;
}

export interface Question {
  id: string;
  quizId: string;
  text: string;
  type: QuestionType;
  options: AnswerOption[];
}

export interface QuizInput {
  title: string;
  description?: string | null;
  folderId?: string | null;
}

export interface OptionInput {
  text: string;
  isCorrect: boolean;
}

export interface QuestionInput {
  text: string;
  type: QuestionType;
  options: OptionInput[];
}

export interface AnswerInput {
  questionId: string;
  selectedOptionIds: string[];
}

export interface AttemptAnswerResult {
  questionId: string;
  isCorrect: boolean;
  selectedOptionIds: string[];
  correctOptionIds: string[];
}

export interface AttemptResult {
  id: string;
  quizId: string;
  score: number;
  maxScore: number;
  percentage: number;
  completedAt: string;
  answers: AttemptAnswerResult[];
}

export interface AttemptSummary {
  id: string;
  quizId: string;
  score: number;
  maxScore: number;
  percentage: number;
  completedAt: string;
}

export const questionTypeLabels: Record<QuestionType, string> = {
  SingleChoice: "Single choice",
  MultipleChoice: "Multiple choice",
  TrueFalse: "True / False",
};
