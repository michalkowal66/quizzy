package com.example.quizzy.controllers;

import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.QuizSubmissionDto;
import com.example.quizzy.services.QuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/start/{quizId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuizAttemptStartResponseDto> startQuizAttempt(@PathVariable Long quizId) {
        QuizAttemptStartResponseDto response = quizAttemptService.startQuiz(quizId);
        return ResponseEntity.ok(response);
    }

    /**
     * Submits answers for a specific quiz attempt.
     * @param attemptId The ID of the quiz attempt being submitted.
     * @param submissionDto The DTO containing the list of answers.
     * @return A no content response indicating success.
     */
    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("@quizAttemptSecurityService.isOwner(#attemptId, authentication)")
    public ResponseEntity<Void> submitAnswers(@PathVariable Long attemptId, @Valid @RequestBody QuizSubmissionDto submissionDto) {
        quizAttemptService.submitAnswers(attemptId, submissionDto);
        return ResponseEntity.noContent().build();
    }
}
