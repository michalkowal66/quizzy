package com.example.quizzy.dto.question;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter @Setter
public class MultipleChoiceQuestionResponseDto extends QuestionResponseDto {
    private List<ChoiceResponseDto> choices;
}
