package com.example.quizzy.dto.question;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PlayableMatchingQuestionDto extends PlayableQuestionDto {
    private List<String> sourceItems;
    private List<String> targetItems;
}
