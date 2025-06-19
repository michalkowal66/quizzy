package com.example.quizzy.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TRUE_FALSE", description = "Request DTO for a True/False question.")
public class TrueFalseQuestionRequestDto extends QuestionRequestDto {
    @Schema(description = "The correct answer for the question.", example = "true")
    private boolean correctAnswer;
}
