package com.example.quizzy.dto.submission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrueFalseAnswerSubmissionDto extends AnswerSubmissionDto {
    private boolean submittedAnswer;
}
