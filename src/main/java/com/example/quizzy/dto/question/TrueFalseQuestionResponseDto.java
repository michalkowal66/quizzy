package com.example.quizzy.dto.question;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class TrueFalseQuestionResponseDto extends QuestionResponseDto {
    private boolean correctAnswer;
}