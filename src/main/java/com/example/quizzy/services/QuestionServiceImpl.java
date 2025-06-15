package com.example.quizzy.services;

import com.example.quizzy.dto.question.MatchingQuestionRequestDto;
import com.example.quizzy.dto.question.MultipleChoiceQuestionRequestDto;
import com.example.quizzy.dto.question.QuestionRequestDto;
import com.example.quizzy.dto.question.TrueFalseQuestionRequestDto;
import com.example.quizzy.entity.*;
import com.example.quizzy.repository.QuestionRepository;
import com.example.quizzy.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
