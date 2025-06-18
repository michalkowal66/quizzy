package com.example.quizzy.dto.submission;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class MatchingAnswerSubmissionDto extends AnswerSubmissionDto {
    @NotEmpty
    private Map<String, String> submittedPairs; // Key: sourceItem, Value: targetItem
}
