package com.example.quizzy.services;

import com.example.quizzy.dto.question.QuestionRequestDto;
import com.example.quizzy.entity.Question;

public interface QuestionService {
    /**
     * Adds a new question to a specific quiz.
     * @param quizId The ID of the quiz to add the question to.
     * @param questionDto The DTO containing the question data.
     * @return The created Question entity.
     */
    Question addQuestionToQuiz(Long quizId, QuestionRequestDto questionDto);
}
