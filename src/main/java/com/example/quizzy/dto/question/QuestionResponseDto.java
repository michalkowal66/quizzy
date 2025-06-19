package com.example.quizzy.dto.question;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

// This structure is very similar to PlayableQuestionDto, but we might want to return different data
// to the author of the quiz (e.g. including correct answers).
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "questionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TrueFalseQuestionResponseDto.class, name = "TRUE_FALSE"),
        @JsonSubTypes.Type(value = MultipleChoiceQuestionResponseDto.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = MatchingQuestionResponseDto.class, name = "MATCHING")
})
public abstract class QuestionResponseDto {
    private Long id;
    private String text;
}