package com.example.quizzy.controllers;

import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.services.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    /**
     * Starts a new attempt for a given quiz.
     * @param quizId The ID of the quiz to start.
     * @return A DTO containing the new attempt ID and a list of playable questions.
     */
    @PostMapping("/quizzes/{quizId}/attempts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuizAttemptStartResponseDto> startQuizAttempt(@PathVariable Long quizId) {
        QuizAttemptStartResponseDto response = quizAttemptService.startQuiz(quizId);
        return ResponseEntity.ok(response);
    }
}
