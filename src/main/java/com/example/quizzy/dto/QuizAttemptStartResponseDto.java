package com.example.quizzy.dto;

import com.example.quizzy.dto.question.PlayableQuestionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * DTO returned to the user when they start a new quiz attempt.
 */
@Getter
@Setter
@AllArgsConstructor
public class QuizAttemptStartResponseDto {
    private Long attemptId;
    private List<PlayableQuestionDto> questions;
}
