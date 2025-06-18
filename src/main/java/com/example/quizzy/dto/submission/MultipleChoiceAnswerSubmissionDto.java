package com.example.quizzy.dto.submission;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class MultipleChoiceAnswerSubmissionDto extends AnswerSubmissionDto {
    @NotEmpty
    private Set<Long> selectedChoiceIds;
}
