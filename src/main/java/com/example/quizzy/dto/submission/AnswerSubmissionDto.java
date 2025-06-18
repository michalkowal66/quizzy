package com.example.quizzy.dto.submission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for submitting an answer to any type of question.
 * It's polymorphic, using the 'questionType' field as the discriminator.
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "questionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TrueFalseAnswerSubmissionDto.class, name = "TRUE_FALSE"),
        @JsonSubTypes.Type(value = MultipleChoiceAnswerSubmissionDto.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = MatchingAnswerSubmissionDto.class, name = "MATCHING")
})
public abstract class AnswerSubmissionDto {
    @NotNull
    private Long questionId;
}
