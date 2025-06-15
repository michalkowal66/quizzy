package com.example.quizzy.dto.question;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrueFalseQuestionRequestDto extends QuestionRequestDto {
    private boolean correctAnswer;
}
