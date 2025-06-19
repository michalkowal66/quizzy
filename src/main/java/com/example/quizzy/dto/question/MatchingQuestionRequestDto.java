package com.example.quizzy.dto.question;

import com.example.quizzy.dto.MatchPairDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Schema(name = "MATCHING", description = "Request DTO for a Matching question.")
public class MatchingQuestionRequestDto extends QuestionRequestDto {
    @NotEmpty
    private List<@Valid MatchPairDto> pairs;
}
