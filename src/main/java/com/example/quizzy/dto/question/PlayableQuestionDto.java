package com.example.quizzy.dto.question;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for presenting any type of question to a user who is taking a quiz.
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "questionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayableMultipleChoiceQuestionDto.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = PlayableTrueFalseQuestionDto.class, name = "TRUE_FALSE"),
        @JsonSubTypes.Type(value = PlayableMatchingQuestionDto.class, name = "MATCHING")
})
public abstract class PlayableQuestionDto {
    private Long id;
    private String text;
}
