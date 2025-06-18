package com.example.quizzy.services;

import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.QuizSubmissionDto;

public interface QuizAttemptService {
    /**
     * Starts a new quiz attempt for the logged-in user.
     * Creates a QuizAttempt entity and returns the questions in a playable format.
     * @param quizId The ID of the quiz to start.
     * @return A DTO containing the attempt ID and the list of playable questions.
     */
    QuizAttemptStartResponseDto startQuiz(Long quizId);

    /**
     * Submits answers for a given quiz attempt.
     * @param attemptId The ID of the quiz attempt.
     * @param submissionDto The DTO containing the user's answers.
     */
    void submitAnswers(Long attemptId, QuizSubmissionDto submissionDto);
}
