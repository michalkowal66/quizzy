package com.example.quizzy.dto.result;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for representing the result of a single answered question.
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "questionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GradedTrueFalseQuestionDto.class, name = "TRUE_FALSE"),
        @JsonSubTypes.Type(value = GradedMultipleChoiceQuestionDto.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = GradedMatchingQuestionDto.class, name = "MATCHING")
})
public abstract class GradedQuestionDto {
    private Long questionId;
    private String text;
    private boolean correct; // Was the user's answer correct?
}
