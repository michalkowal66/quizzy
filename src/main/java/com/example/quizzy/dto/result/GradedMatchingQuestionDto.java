package com.example.quizzy.dto.result;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class GradedMatchingQuestionDto extends GradedQuestionDto {
    private Map<String, String> submittedPairs;
    private Map<String, String> correctPairs;
}