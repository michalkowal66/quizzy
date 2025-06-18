package com.example.quizzy.dto.question;

import com.example.quizzy.dto.PlayableChoiceDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PlayableMultipleChoiceQuestionDto extends PlayableQuestionDto {
    private List<PlayableChoiceDto> choices;
}
