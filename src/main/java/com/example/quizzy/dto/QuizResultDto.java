package com.example.quizzy.dto;

import com.example.quizzy.dto.result.GradedQuestionDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO representing the final result of a quiz attempt.
 */
@Getter
@Builder // Using Builder pattern for convenient construction
public class QuizResultDto {
    private Long attemptId;
    private String quizTitle;
    private int totalQuestions;
    private int correctAnswersCount;
    private double score;
    private List<GradedQuestionDto> gradedQuestions;
}
