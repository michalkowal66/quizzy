package com.example.quizzy.dto.result;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class GradedMultipleChoiceQuestionDto extends GradedQuestionDto {
    private Set<String> submittedChoices;
    private Set<String> correctChoices;
}