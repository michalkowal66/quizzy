package com.example.quizzy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for a choice in a multiple-choice question, presented to the user.
 */
@Getter
@Setter
@AllArgsConstructor
public class PlayableChoiceDto {
    private Long id;
    private String text;
}
