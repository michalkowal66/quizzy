package com.example.quizzy.dto.question;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class MatchPairResponseDto {
    private Long id;
    private String sourceItem;
    private String targetItem;
}
