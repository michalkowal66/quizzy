package com.example.quizzy.services;

import com.example.quizzy.dto.question.*;
import com.example.quizzy.entity.*;
import com.example.quizzy.repository.QuestionRepository;
import com.example.quizzy.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    @Override
    public Question addQuestionToQuiz(Long quizId, QuestionRequestDto dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        Question question = mapDtoToEntity(dto);
        question.setQuiz(quiz);

        return questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        // Check if the question exists before attempting to delete
        if (!questionRepository.existsById(questionId)) {
            throw new RuntimeException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    @Override
    @Transactional
    public QuestionResponseDto updateQuestion(Long questionId, QuestionRequestDto dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        // Update common field
        question.setText(dto.getText());

        // Update type-specific fields, ensuring the type is not changed
        if (question instanceof TrueFalseQuestion tfQuestion && dto instanceof TrueFalseQuestionRequestDto tfDto) {
            tfQuestion.setCorrectAnswer(tfDto.isCorrectAnswer());
        } else if (question instanceof MultipleChoiceQuestion mcQuestion && dto instanceof MultipleChoiceQuestionRequestDto mcDto) {
            // Update a collection by clear and re-add
            mcQuestion.getChoices().clear();
            mcDto.getChoices().forEach(choiceDto -> {
                Choice choice = new Choice();
                choice.setText(choiceDto.getText());
                choice.setCorrect(choiceDto.isCorrect());
                mcQuestion.getChoices().add(choice);
            });
        } else if (question instanceof MatchingQuestion mQuestion && dto instanceof MatchingQuestionRequestDto mDto) {
            mQuestion.getPairs().clear();
            mDto.getPairs().forEach(pairDto -> {
                MatchPair pair = new MatchPair();
                pair.setSourceItem(pairDto.getSourceItem());
                pair.setTargetItem(pairDto.getTargetItem());
                mQuestion.getPairs().add(pair);
            });
        } else {
            // If the types do not match, throw an exception
            throw new IllegalArgumentException("Cannot change the type of an existing question.");
        }

        Question updatedQuestion = questionRepository.save(question);
        return mapEntityToResponseDto(updatedQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponseDto> getQuestionsForQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        return quiz.getQuestions().stream()
                .map(this::mapEntityToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponseDto getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        return mapEntityToResponseDto(question);
    }

    /**
     * Maps a QuestionRequestDto to the corresponding Question entity based on its type.
     * @param dto The polymorphic DTO.
     * @return A Question entity subclass.
     */
    private Question mapDtoToEntity(QuestionRequestDto dto) {
        Question question;

        // Jackson's polymorphic deserialization
        if (dto instanceof MultipleChoiceQuestionRequestDto mcDto) {
            MultipleChoiceQuestion mcQuestion = new MultipleChoiceQuestion();
            mcQuestion.setChoices(mcDto.getChoices().stream().map(choiceDto -> {
                Choice choice = new Choice();
                choice.setText(choiceDto.getText());
                choice.setCorrect(choiceDto.isCorrect());
                return choice;
            }).collect(Collectors.toList()));
            question = mcQuestion;
        } else if (dto instanceof TrueFalseQuestionRequestDto tfDto) {
            TrueFalseQuestion tfQuestion = new TrueFalseQuestion();
            tfQuestion.setCorrectAnswer(tfDto.isCorrectAnswer());
            question = tfQuestion;
        } else if (dto instanceof MatchingQuestionRequestDto mDto) {
            MatchingQuestion mQuestion = new MatchingQuestion();
            mQuestion.setPairs(mDto.getPairs().stream().map(pairDto -> {
                MatchPair pair = new MatchPair();
                pair.setSourceItem(pairDto.getSourceItem());
                pair.setTargetItem(pairDto.getTargetItem());
                return pair;
            }).collect(Collectors.toList()));
            question = mQuestion;
        } else {
            throw new IllegalArgumentException("Unknown question type");
        }

        question.setText(dto.getText());
        return question;
    }

    /**
     * Maps a Question entity to its corresponding QuestionResponseDto.
     * This method handles the polymorphic nature of questions, creating the correct
     * DTO subclass for each entity type.
     *
     * @param question The Question entity from the database.
     * @return The corresponding DTO to be sent in the API response.
     */
    private QuestionResponseDto mapEntityToResponseDto(Question question) {
        QuestionResponseDto dto;

        if (question instanceof TrueFalseQuestion tfQuestion) {
            TrueFalseQuestionResponseDto specificDto = new TrueFalseQuestionResponseDto();
            specificDto.setCorrectAnswer(tfQuestion.isCorrectAnswer());
            dto = specificDto;

        } else if (question instanceof MultipleChoiceQuestion mcQuestion) {
            MultipleChoiceQuestionResponseDto specificDto = new MultipleChoiceQuestionResponseDto();
            // Map the list of Choice entities to a list of ChoiceResponseDto
            specificDto.setChoices(mcQuestion.getChoices().stream().map(choice -> {
                ChoiceResponseDto choiceDto = new ChoiceResponseDto();
                choiceDto.setId(choice.getId());
                choiceDto.setText(choice.getText());
                choiceDto.setCorrect(choice.isCorrect());
                return choiceDto;
            }).collect(Collectors.toList()));
            dto = specificDto;

        } else if (question instanceof MatchingQuestion mQuestion) {
            MatchingQuestionResponseDto specificDto = new MatchingQuestionResponseDto();
            // Map the list of MatchPair entities to a list of MatchPairResponseDto
            specificDto.setPairs(mQuestion.getPairs().stream().map(pair -> {
                MatchPairResponseDto pairDto = new MatchPairResponseDto();
                pairDto.setId(pair.getId());
                pairDto.setSourceItem(pair.getSourceItem());
                pairDto.setTargetItem(pair.getTargetItem());
                return pairDto;
            }).collect(Collectors.toList()));
            dto = specificDto;

        } else {
            throw new IllegalArgumentException("Unknown Question type for DTO mapping: " + question.getClass().getName());
        }

        // Set the common properties from the base Question entity
        dto.setId(question.getId());
        dto.setText(question.getText());

        return dto;
    }
}
