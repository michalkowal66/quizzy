package com.example.quizzy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchPairDto {
    @NotBlank
    private String sourceItem;
    @NotBlank
    private String targetItem;
}
