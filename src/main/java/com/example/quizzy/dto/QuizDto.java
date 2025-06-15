package com.example.quizzy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for creating and updating a Quiz.
 */
@Getter
@Setter
public class QuizDto {
    @NotBlank(message = "Title is required.")
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 1000)
    private String description;
}
