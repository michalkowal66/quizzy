package com.example.quizzy.services;

import com.example.quizzy.dto.QuizDto;
import com.example.quizzy.dto.QuizResponseDto;
import java.util.List;

public interface QuizService {
    QuizResponseDto createQuiz(QuizDto quizDto);
    QuizResponseDto getQuizById(Long quizId);
    List<QuizResponseDto> getCurrentUserQuizzes();
    QuizResponseDto updateQuiz(Long quizId, QuizDto quizDto);
    void deleteQuiz(Long quizId);
}
