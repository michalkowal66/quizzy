package com.example.quizzy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceDto {
    @NotBlank
    private String text;
    private boolean correct;
}
