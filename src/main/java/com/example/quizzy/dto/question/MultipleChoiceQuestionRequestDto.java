package com.example.quizzy.dto.question;

import com.example.quizzy.dto.ChoiceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MultipleChoiceQuestionRequestDto extends QuestionRequestDto {
    @NotEmpty
    @Size(min = 2)
    private List<@Valid ChoiceDto> choices;
}
