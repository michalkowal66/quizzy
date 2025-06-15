package com.example.quizzy.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for returning Quiz data to the client.
 */
@Getter
@Setter
public class QuizResponseDto {
    private Long id;
    private String title;
    private String description;
    private String authorUsername;
}
