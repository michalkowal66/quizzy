package com.example.quizzy.dto.submission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        description = "Base DTO for submitting an answer to a question.",
        discriminatorProperty = "questionType",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "TRUE_FALSE", schema = TrueFalseAnswerSubmissionDto.class),
                @DiscriminatorMapping(value = "MULTIPLE_CHOICE", schema = MultipleChoiceAnswerSubmissionDto.class),
                @DiscriminatorMapping(value = "MATCHING", schema = MatchingAnswerSubmissionDto.class)
        }
)
public abstract class AnswerSubmissionDto {
    @NotNull
    @Schema(description = "The ID of the question being answered.", example = "1")
    private Long questionId;
}
