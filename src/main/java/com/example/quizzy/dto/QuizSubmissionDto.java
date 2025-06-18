package com.example.quizzy.dto;

import com.example.quizzy.dto.submission.AnswerSubmissionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * DTO representing a user's full submission for a quiz attempt.
 */
@Getter
@Setter
public class QuizSubmissionDto {
    @NotEmpty
    private List<@Valid AnswerSubmissionDto> answers;
}