package com.example.quizzy.dto.question;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class ChoiceResponseDto {
    private Long id;
    private String text;
    private boolean isCorrect;
}
