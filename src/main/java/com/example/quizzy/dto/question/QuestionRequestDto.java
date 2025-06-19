package com.example.quizzy.dto.question;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        description = "Base request object for creating a question.",
        discriminatorProperty = "questionType", // This is the field that determines the object type
        discriminatorMapping = {
                @DiscriminatorMapping(value = "TRUE_FALSE", schema = TrueFalseQuestionRequestDto.class),
                @DiscriminatorMapping(value = "MULTIPLE_CHOICE", schema = MultipleChoiceQuestionRequestDto.class),
                @DiscriminatorMapping(value = "MATCHING", schema = MatchingQuestionRequestDto.class)
        }
)
public abstract class QuestionRequestDto {
    @NotBlank
    @Schema(description = "The main text or prompt for the question.", example = "What is the capital of Poland?")
    private String text;
}
