package com.example.quizzy.controllers;

import com.example.quizzy.dto.QuizDto;
import com.example.quizzy.dto.QuizResponseDto;
import com.example.quizzy.services.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * Creates a new quiz for the authenticated user.
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
     * @return A list of the user's quizzes.
     */
    @GetMapping("/my-quizzes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<QuizResponseDto>> getCurrentUserQuizzes() {
        return ResponseEntity.ok(quizService.getCurrentUserQuizzes());
    }

    /**
     * Retrieves a single quiz by its ID.
     * This endpoint is public.
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
     * @param id The ID of the quiz to delete.
     * @return A no content response.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @quizSecurityService.isOwner(#id, authentication)")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
