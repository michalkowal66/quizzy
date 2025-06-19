package com.example.quizzy.controllers;

import com.example.quizzy.dto.*;
import com.example.quizzy.dto.question.*;
import com.example.quizzy.services.QuestionService;
import com.example.quizzy.services.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Unit tests for the QuizController class.
 * <p>
 * These tests focus on the controller's logic, such as request mapping,
 * data binding, and delegation to service layers.
 */
@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QuizService quizService;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuizController quizController;

    /**
     * A simple exception handler to simulate a global one.
     * It catches exceptions thrown by mocked services and translates them
     * into appropriate HTTP status codes, allowing for testing of failure scenarios.
     */
    @ControllerAdvice
    static class TestControllerAdvice {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleNotFound(RuntimeException ex) {
            // For simplicity generic RuntimeException is mapped to 404 Not Found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(quizController)
                .setControllerAdvice(new TestControllerAdvice())
                .build();
    }

    @Nested
    @DisplayName("Quiz Management Tests")
    class QuizManagement {
        @Test
        @DisplayName("POST /api/quizzes - Should create a new quiz and return 201 Created")
        void createQuiz_shouldReturnCreatedQuiz() throws Exception {
            // Arrange
            QuizDto quizDto = new QuizDto();
            quizDto.setTitle("New Quiz");

            QuizResponseDto expectedResponse = new QuizResponseDto();
            expectedResponse.setId(1L);
            expectedResponse.setTitle("New Quiz");

            when(quizService.createQuiz(any(QuizDto.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post("/api/quizzes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(quizDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("New Quiz")));

            verify(quizService).createQuiz(any(QuizDto.class));
        }

        @Test
        @DisplayName("GET /api/quizzes/my-quizzes - Should return a list of the user's quizzes")
        void getCurrentUserQuizzes_shouldReturnListOfQuizzes() throws Exception {
            // Arrange
            QuizResponseDto quiz1 = new QuizResponseDto();
            quiz1.setId(1L);
            quiz1.setTitle("My First Quiz");
            when(quizService.getCurrentUserQuizzes()).thenReturn(List.of(quiz1));

            // Act & Assert
            mockMvc.perform(get("/api/quizzes/my-quizzes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("My First Quiz")));

            verify(quizService).getCurrentUserQuizzes();
        }

        @Test
        @DisplayName("GET /api/quizzes/{id} - Should return a quiz when it exists")
        void getQuizById_whenExists_shouldReturnQuiz() throws Exception {
            // Arrange
            long quizId = 1L;
            QuizResponseDto expectedResponse = new QuizResponseDto();
            expectedResponse.setId(quizId);
            expectedResponse.setTitle("Existing Quiz");
            when(quizService.getQuizById(quizId)).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/api/quizzes/{id}", quizId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));

            verify(quizService).getQuizById(quizId);
        }

        @Test
        @DisplayName("GET /api/quizzes/{id} - Should return 404 Not Found when quiz does not exist")
        void getQuizById_whenNotExists_shouldReturnNotFound() throws Exception {
            // Arrange
            long quizId = 99L;
            when(quizService.getQuizById(quizId)).thenThrow(new RuntimeException("Quiz not found"));

            // Act & Assert
            mockMvc.perform(get("/api/quizzes/{id}", quizId))
                    .andExpect(status().isNotFound());

            verify(quizService).getQuizById(quizId);
        }

        @Test
        @DisplayName("DELETE /api/quizzes/{id} - Should delete quiz and return 204 No Content")
        void deleteQuiz_whenSuccessful_shouldReturnNoContent() throws Exception {
            // Arrange
            long quizId = 1L;
            doNothing().when(quizService).deleteQuiz(quizId);

            // Act & Assert
            mockMvc.perform(delete("/api/quizzes/{id}", quizId))
                    .andExpect(status().isNoContent());

            verify(quizService).deleteQuiz(quizId);
        }
    }

    @Nested
    @DisplayName("Question Management Tests")
    class QuestionManagement {

        @Test
        @DisplayName("POST /{quizId}/questions - Should add a question to a quiz and return 201 Created")
        void addQuestionToQuiz_shouldReturnCreated() throws Exception {
            // Arrange
            long quizId = 1L;
            TrueFalseQuestionRequestDto questionDto = new TrueFalseQuestionRequestDto();
            questionDto.setText("Is this a test?");
            questionDto.setCorrectAnswer(true);

            when(questionService.addQuestionToQuiz(eq(quizId), any(QuestionRequestDto.class))).thenReturn(null);

            // Act & Assert
            mockMvc.perform(post("/api/quizzes/{quizId}/questions", quizId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("Question added successfully to quiz with id: " + quizId));

            verify(questionService).addQuestionToQuiz(eq(quizId), any(QuestionRequestDto.class));
        }

        @Test
        @DisplayName("DELETE /{quizId}/questions/{questionId} - Should delete a question and return 204 No Content")
        void deleteQuestion_shouldReturnNoContent() throws Exception {
            // Arrange
            long quizId = 1L;
            long questionId = 10L;
            doNothing().when(questionService).deleteQuestion(questionId);

            // Act & Assert
            mockMvc.perform(delete("/api/quizzes/{quizId}/questions/{questionId}", quizId, questionId))
                    .andExpect(status().isNoContent());

            verify(questionService).deleteQuestion(questionId);
        }

        @Test
        @DisplayName("PUT /{quizId}/questions/{questionId} - Should update question and return 200 OK")
        void updateQuestion_shouldReturnUpdatedQuestion() throws Exception {
            // Arrange
            long quizId = 1L;
            long questionId = 10L;

            // Request DTO
            TrueFalseQuestionRequestDto requestDto = new TrueFalseQuestionRequestDto();
            requestDto.setText("Updated text?");
            requestDto.setCorrectAnswer(false);

            // Expected Response DTO
            TrueFalseQuestionResponseDto responseDto = new TrueFalseQuestionResponseDto();
            responseDto.setId(questionId);
            responseDto.setText("Updated text?");
            responseDto.setCorrectAnswer(false);

            when(questionService.updateQuestion(eq(questionId), any(QuestionRequestDto.class))).thenReturn(responseDto);

            // Act & Assert
            mockMvc.perform(put("/api/quizzes/{quizId}/questions/{questionId}", quizId, questionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is((int)questionId)))
                    .andExpect(jsonPath("$.text", is("Updated text?")));

            verify(questionService).updateQuestion(eq(questionId), any(QuestionRequestDto.class));
        }

        @Test
        @DisplayName("GET /{quizId}/questions - Should return list of questions for a quiz")
        void getQuestionsForQuiz_shouldReturnListOfQuestions() throws Exception {
            // Arrange
            long quizId = 1L;

            TrueFalseQuestionResponseDto question1 = new TrueFalseQuestionResponseDto();
            question1.setId(10L);
            question1.setText("First question");

            when(questionService.getQuestionsForQuiz(quizId)).thenReturn(List.of(question1));

            // Act & Assert
            mockMvc.perform(get("/api/quizzes/{quizId}/questions", quizId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(10)));

            verify(questionService).getQuestionsForQuiz(quizId);
        }
    }
}