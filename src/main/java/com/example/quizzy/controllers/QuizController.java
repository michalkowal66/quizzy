package com.example.quizzy.controllers;

import com.example.quizzy.dto.QuizDto;
import com.example.quizzy.dto.QuizResponseDto;
import com.example.quizzy.dto.question.QuestionRequestDto;
import com.example.quizzy.services.QuestionService;
import com.example.quizzy.services.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing quizzes.
 * Provides endpoints for creating, retrieving, updating, and deleting quizzes,
 * as well as for adding questions to them.
 */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuestionService questionService;

    /**
     * Creates a new quiz for the authenticated user.
     *
     * @param quizDto DTO containing quiz title and description.
     * @return The created quiz data.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuizResponseDto> createQuiz(@Valid @RequestBody QuizDto quizDto) {
        QuizResponseDto createdQuiz = quizService.createQuiz(quizDto);
        return new ResponseEntity<>(createdQuiz, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of quizzes created by the currently authenticated user.
     *
     * @return A list of the user's quizzes.
     */
    @GetMapping("/my-quizzes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<QuizResponseDto>> getCurrentUserQuizzes() {
        return ResponseEntity.ok(quizService.getCurrentUserQuizzes());
    }

    /**
     * Retrieves a single quiz by its ID.
     * This endpoint is public and does not require authentication.
     *
     * @param id The ID of the quiz.
     * @return The quiz data.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponseDto> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    /**
     * Updates an existing quiz.
     * Access is restricted to the owner of the quiz or an admin.
     *
     * @param id The ID of the quiz to update.
     * @param quizDto DTO with new quiz data.
     * @return The updated quiz data.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @quizSecurityService.isOwner(#id, authentication)")
    public ResponseEntity<QuizResponseDto> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizDto quizDto) {
        QuizResponseDto updatedQuiz = quizService.updateQuiz(id, quizDto);
        return ResponseEntity.ok(updatedQuiz);
    }

    /**
     * Deletes a quiz.
     * Access is restricted to the owner of the quiz or an admin.
     *
     * @param id The ID of the quiz to delete.
     * @return A no content response.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @quizSecurityService.isOwner(#id, authentication)")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a new question to an existing quiz.
     * The request body must contain a 'questionType' field to determine the question type.
     * Access is restricted to the owner of the quiz or an admin.
     *
     * @param quizId The ID of the quiz.
     * @param questionDto The DTO for the new question.
     * @return A success message.
     */
    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('ADMIN') or @quizSecurityService.isOwner(#quizId, authentication)")
    public ResponseEntity<String> addQuestionToQuiz(@PathVariable Long quizId, @Valid @RequestBody QuestionRequestDto questionDto) {
        questionService.addQuestionToQuiz(quizId, questionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Question added successfully to quiz with id: " + quizId);
    }
}
