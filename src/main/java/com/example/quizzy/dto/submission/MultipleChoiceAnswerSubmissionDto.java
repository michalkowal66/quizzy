package com.example.quizzy.dto.submission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Schema(name = "MULTIPLE_CHOICE", description = "DTO for submitting a Multiple Choice answer.")
public class MultipleChoiceAnswerSubmissionDto extends AnswerSubmissionDto {
    @NotEmpty
    @Schema(description = "A set of IDs for the choices selected by the user.", example = "[2, 4]")
    private Set<Long> selectedChoiceIds;
}
