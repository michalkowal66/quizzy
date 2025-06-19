package com.example.quizzy.controllers;

import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.QuizResultDto;
import com.example.quizzy.dto.QuizSubmissionDto;
import com.example.quizzy.dto.question.PlayableQuestionDto;
import com.example.quizzy.dto.question.PlayableTrueFalseQuestionDto;
import com.example.quizzy.dto.submission.TrueFalseAnswerSubmissionDto;
import com.example.quizzy.services.QuizAttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the QuizAttemptController.
 * <p>
 * These tests verify the controller's logic for starting and submitting quiz attempts.
 * The service layer is mocked to isolate the controller.
 */
@ExtendWith(MockitoExtension.class)
class QuizAttemptControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QuizAttemptService quizAttemptService;

    @InjectMocks
    private QuizAttemptController quizAttemptController;

    /**
     * A minimal, self-contained exception handler for testing failure scenarios.
     */
    @ControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleNotFound(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(quizAttemptController)
                .setControllerAdvice(new TestControllerAdvice())
                .build();
    }

    @Test
    @DisplayName("POST /api/start/{quizId} - Should start a quiz attempt and return attempt details")
    void startQuizAttempt_whenQuizExists_shouldReturnAttemptDetails() throws Exception {
        // Arrange
        long quizId = 1L;
        long attemptId = 100L;

        // Create a sample playable question
        PlayableQuestionDto playableQuestion = new PlayableTrueFalseQuestionDto();
        playableQuestion.setId(10L);
        playableQuestion.setText("Is the sky blue?");

        // Create the expected response from the service
        QuizAttemptStartResponseDto expectedResponse = new QuizAttemptStartResponseDto(attemptId, List.of(playableQuestion));
        when(quizAttemptService.startQuiz(quizId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/start/{quizId}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId", is((int) attemptId)))
                .andExpect(jsonPath("$.questions", hasSize(1)))
                .andExpect(jsonPath("$.questions[0].id", is(10)))
                .andExpect(jsonPath("$.questions[0].text", is("Is the sky blue?")));

        verify(quizAttemptService).startQuiz(quizId);
    }

    @Test
    @DisplayName("POST /api/start/{quizId} - Should return 404 Not Found if quiz does not exist")
    void startQuizAttempt_whenQuizDoesNotExist_shouldReturnNotFound() throws Exception {
        // Arrange
        long quizId = 99L; // A non-existent quiz ID
        when(quizAttemptService.startQuiz(quizId)).thenThrow(new RuntimeException("Quiz not found"));

        // Act & Assert
        mockMvc.perform(post("/api/start/{quizId}", quizId))
                .andExpect(status().isNotFound());

        verify(quizAttemptService).startQuiz(quizId);
    }

    @Test
    @DisplayName("POST /{attemptId}/submit - Should submit answers and return quiz results")
    void submitAnswers_whenAttemptIsValid_shouldReturnResults() throws Exception {
        // Arrange
        long attemptId = 100L;

        // Create a sample submission DTO
        TrueFalseAnswerSubmissionDto answerDto = new TrueFalseAnswerSubmissionDto();
        answerDto.setQuestionId(10L);
        answerDto.setSubmittedAnswer(true);
        QuizSubmissionDto submissionDto = new QuizSubmissionDto();
        submissionDto.setAnswers(List.of(answerDto));

        // Create the expected result DTO from the service
        QuizResultDto expectedResult = QuizResultDto.builder()
                .attemptId(attemptId)
                .quizTitle("My First Quiz")
                .totalQuestions(1)
                .correctAnswersCount(1)
                .score(100.0)
                .gradedQuestions(List.of()) // For simplicity, graded questions list is not checked
                .build();

        when(quizAttemptService.submitAnswers(eq(attemptId), any(QuizSubmissionDto.class))).thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/api/{attemptId}/submit", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId", is((int) attemptId)))
                .andExpect(jsonPath("$.score", is(100.0)))
                .andExpect(jsonPath("$.correctAnswersCount", is(1)));

        verify(quizAttemptService).submitAnswers(eq(attemptId), any(QuizSubmissionDto.class));
    }

    @Test
    @DisplayName("POST /{attemptId}/submit - Should return 409 Conflict if attempt is already submitted")
    void submitAnswers_whenAttemptAlreadySubmitted_shouldReturnConflict() throws Exception {
        // Arrange
        long attemptId = 101L;

        // Create a sample submission DTO
        TrueFalseAnswerSubmissionDto answerDto = new TrueFalseAnswerSubmissionDto();
        answerDto.setQuestionId(10L); // Set required field
        answerDto.setSubmittedAnswer(true);
        QuizSubmissionDto submissionDto = new QuizSubmissionDto();
        submissionDto.setAnswers(List.of(answerDto));

        when(quizAttemptService.submitAnswers(eq(attemptId), any(QuizSubmissionDto.class)))
                .thenThrow(new IllegalStateException("Quiz attempt has already been submitted."));

        // Act & Assert
        mockMvc.perform(post("/api/{attemptId}/submit", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isConflict());

        verify(quizAttemptService).submitAnswers(eq(attemptId), any(QuizSubmissionDto.class));
    }
}