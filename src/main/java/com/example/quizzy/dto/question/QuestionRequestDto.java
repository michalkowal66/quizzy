package com.example.quizzy.dto.question;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for creating any type of question.
 * Jackson annotations are used to deserialize to the correct subclass based on the 'questionType' property.
 */
@Getter
@Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "questionType" // This field will determine the subclass
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MultipleChoiceQuestionRequestDto.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = TrueFalseQuestionRequestDto.class, name = "TRUE_FALSE"),
        @JsonSubTypes.Type(value = MatchingQuestionRequestDto.class, name = "MATCHING")
})
public abstract class QuestionRequestDto {
    @NotBlank
    private String text;
}
