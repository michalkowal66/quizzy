package com.example.quizzy.dto.question;

import com.example.quizzy.dto.MatchPairDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MatchingQuestionRequestDto extends QuestionRequestDto {
    @NotEmpty
    private List<@Valid MatchPairDto> pairs;
}
