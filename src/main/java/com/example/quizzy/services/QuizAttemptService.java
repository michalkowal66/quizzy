package com.example.quizzy.services;

import com.example.quizzy.dto.QuizAttemptStartResponseDto;

public interface QuizAttemptService {
    /**
     * Starts a new quiz attempt for the logged-in user.
     * Creates a QuizAttempt entity and returns the questions in a playable format.
     * @param quizId The ID of the quiz to start.
     * @return A DTO containing the attempt ID and the list of playable questions.
     */
    QuizAttemptStartResponseDto startQuiz(Long quizId);
}
