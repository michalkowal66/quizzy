package com.example.quizzy.dto.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradedTrueFalseQuestionDto extends GradedQuestionDto {
    private boolean submittedAnswer;
    private boolean correctAnswer;
}
