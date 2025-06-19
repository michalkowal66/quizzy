package com.example.quizzy.services;

import com.example.quizzy.dto.question.QuestionRequestDto;
import com.example.quizzy.dto.question.QuestionResponseDto;
import com.example.quizzy.entity.Question;

import java.util.List;

public interface QuestionService {
    /**
     * Adds a new question to a specific quiz.
     * @param quizId The ID of the quiz to add the question to.
     * @param questionDto The DTO containing the question data.
     * @return The created Question entity.
     */
    Question addQuestionToQuiz(Long quizId, QuestionRequestDto questionDto);

    /**
     * Deletes a question by its ID.
     * The security check (ownership) is handled at the controller level.
     * @param questionId The ID of the question to delete.
     */
    void deleteQuestion(Long questionId);

    /**
     * Updates an existing question.
     * The type of the question cannot be changed.
     * @param questionId The ID of the question to update.
     * @param questionDto DTO with the new data for the question.
     * @return A DTO of the updated question.
     */
    QuestionResponseDto updateQuestion(Long questionId, QuestionRequestDto questionDto);

    /**
     * Retrieves all questions for a specific quiz.
     *
     * @param quizId The ID of the quiz.
     * @return A list of DTOs representing the questions.
     */
    List<QuestionResponseDto> getQuestionsForQuiz(Long quizId);

    /**
     * Retrieves a single question by its ID.
     *
     * @param questionId The ID of the question.
     * @return A DTO representing the question.
     */
    QuestionResponseDto getQuestionById(Long questionId);
}
