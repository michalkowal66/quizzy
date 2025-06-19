package com.example.quizzy.dto.submission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TRUE_FALSE", description = "DTO for submitting a True/False answer.")
public class TrueFalseAnswerSubmissionDto extends AnswerSubmissionDto {
    @Schema(description = "The boolean answer submitted by the user.", example = "true")
    private boolean submittedAnswer;
}
