package com.example.quizzy.dto.submission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
@Schema(name = "MATCHING", description = "DTO for submitting a Matching answer.")
public class MatchingAnswerSubmissionDto extends AnswerSubmissionDto {
    @NotEmpty
    @Schema(description = "A map representing the pairs matched by the user.",
            example = "{\"Germany\": \"Berlin\", \"France\": \"Paris\"}")
    private Map<String, String> submittedPairs; // Key: sourceItem, Value: targetItem
}
