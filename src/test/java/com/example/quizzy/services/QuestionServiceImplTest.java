package com.example.quizzy.services;

import com.example.quizzy.dto.ChoiceDto;
import com.example.quizzy.dto.MatchPairDto;
import com.example.quizzy.dto.question.*;
import com.example.quizzy.entity.*;
import com.example.quizzy.repository.QuestionRepository;
import com.example.quizzy.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Question Service Implementation Tests")
class QuestionServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Quiz testQuiz;
    private final Long QUIZ_ID = 1L;
    private final Long QUESTION_ID = 10L;

    @BeforeEach
    void setUp() {
        testQuiz = new Quiz();
        testQuiz.setId(QUIZ_ID);
        testQuiz.setTitle("Test Quiz");
    }

    @Test
    @DisplayName("addQuestionToQuiz should save a TrueFalseQuestion correctly")
    void addQuestionToQuiz_withTrueFalseDto_shouldSaveCorrectEntity() {
        TrueFalseQuestionRequestDto dto = new TrueFalseQuestionRequestDto();
        dto.setText("Is the sky blue?");
        dto.setCorrectAnswer(true);
        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(testQuiz));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        Question savedQuestion = questionService.addQuestionToQuiz(QUIZ_ID, dto);

        assertNotNull(savedQuestion);
        assertTrue(savedQuestion instanceof TrueFalseQuestion);
        assertEquals("Is the sky blue?", savedQuestion.getText());
        assertEquals(testQuiz, savedQuestion.getQuiz());
        assertTrue(((TrueFalseQuestion) savedQuestion).isCorrectAnswer());
        verify(questionRepository, times(1)).save(any(TrueFalseQuestion.class));
    }

    @Test
    @DisplayName("addQuestionToQuiz should save a MultipleChoiceQuestion correctly")
    void addQuestionToQuiz_withMultipleChoiceDto_shouldSaveCorrectEntity() {
        MultipleChoiceQuestionRequestDto dto = new MultipleChoiceQuestionRequestDto();
        dto.setText("Which number is prime?");
        ChoiceDto choiceDto = new ChoiceDto();
        choiceDto.setText("7");
        choiceDto.setCorrect(true);
        dto.setChoices(List.of(choiceDto));
        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(testQuiz));

        questionService.addQuestionToQuiz(QUIZ_ID, dto);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        assertTrue(questionCaptor.getValue() instanceof MultipleChoiceQuestion);
    }

    @Test
    @DisplayName("addQuestionToQuiz should save a MatchingQuestion correctly")
    void addQuestionToQuiz_withMatchingDto_shouldSaveCorrectEntity() {
        MatchingQuestionRequestDto dto = new MatchingQuestionRequestDto();
        dto.setText("Match symbols");
        MatchPairDto pairDto = new MatchPairDto();
        pairDto.setSourceItem("Au");
        pairDto.setTargetItem("Gold");
        dto.setPairs(List.of(pairDto));
        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(testQuiz));

        questionService.addQuestionToQuiz(QUIZ_ID, dto);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        assertTrue(questionCaptor.getValue() instanceof MatchingQuestion);
    }

    @Test
    @DisplayName("addQuestionToQuiz should throw exception for unknown DTO type to cover mapDtoToEntity else branch")
    void addQuestionToQuiz_withUnknownDtoType_shouldThrowException() {
        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(testQuiz));
        QuestionRequestDto unknownDto = mock(QuestionRequestDto.class);

        assertThrows(IllegalArgumentException.class, () -> questionService.addQuestionToQuiz(QUIZ_ID, unknownDto));
    }

    @Test
    @DisplayName("deleteQuestion should call repository delete method when question exists")
    void deleteQuestion_whenQuestionExists_shouldCallDelete() {
        when(questionRepository.existsById(QUESTION_ID)).thenReturn(true);
        doNothing().when(questionRepository).deleteById(QUESTION_ID);

        questionService.deleteQuestion(QUESTION_ID);

        verify(questionRepository, times(1)).existsById(QUESTION_ID);
        verify(questionRepository, times(1)).deleteById(QUESTION_ID);
    }

    @Test
    @DisplayName("deleteQuestion should throw exception when question does not exist")
    void deleteQuestion_whenQuestionNotFound_shouldThrowException() {
        when(questionRepository.existsById(QUESTION_ID)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> questionService.deleteQuestion(QUESTION_ID));
        verify(questionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("updateQuestion for TrueFalseQuestion should update correctly")
    void updateQuestion_withTrueFalseQuestion_shouldUpdateAndReturnDto() {
        TrueFalseQuestion existingQuestion = new TrueFalseQuestion();
        existingQuestion.setId(QUESTION_ID);
        existingQuestion.setText("Old text");
        existingQuestion.setCorrectAnswer(false);
        TrueFalseQuestionRequestDto updateDto = new TrueFalseQuestionRequestDto();
        updateDto.setText("New updated text");
        updateDto.setCorrectAnswer(true);
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(existingQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestionResponseDto responseDto = questionService.updateQuestion(QUESTION_ID, updateDto);

        assertNotNull(responseDto);
        assertEquals("New updated text", responseDto.getText());
        assertTrue(((TrueFalseQuestionResponseDto) responseDto).isCorrectAnswer());
    }

    @Test
    @DisplayName("updateQuestion for MultipleChoiceQuestion should update choices")
    void updateQuestion_withMultipleChoiceQuestion_shouldUpdateChoices() {
        MultipleChoiceQuestion existingQuestion = new MultipleChoiceQuestion();
        existingQuestion.setChoices(new ArrayList<>());
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(existingQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));
        MultipleChoiceQuestionRequestDto updateDto = new MultipleChoiceQuestionRequestDto();
        updateDto.setText("New MC text");
        ChoiceDto newChoice = new ChoiceDto();
        newChoice.setText("New Choice");
        updateDto.setChoices(Collections.singletonList(newChoice));

        questionService.updateQuestion(QUESTION_ID, updateDto);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        assertEquals(1, ((MultipleChoiceQuestion) questionCaptor.getValue()).getChoices().size());
    }

    @Test
    @DisplayName("updateQuestion for MatchingQuestion should update pairs")
    void updateQuestion_withMatchingQuestion_shouldUpdatePairs() {
        MatchingQuestion existingQuestion = new MatchingQuestion();
        existingQuestion.setPairs(new ArrayList<>());
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(existingQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));
        MatchingQuestionRequestDto updateDto = new MatchingQuestionRequestDto();
        updateDto.setText("New matching text");
        MatchPairDto newPair = new MatchPairDto();
        newPair.setSourceItem("A");
        updateDto.setPairs(Collections.singletonList(newPair));

        questionService.updateQuestion(QUESTION_ID, updateDto);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());
        assertEquals(1, ((MatchingQuestion) questionCaptor.getValue()).getPairs().size());
    }

    @Test
    @DisplayName("updateQuestion should throw exception when question is not found")
    void updateQuestion_whenQuestionNotFound_shouldThrowException() {
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> questionService.updateQuestion(QUESTION_ID, new TrueFalseQuestionRequestDto()));
    }

    @Test
    @DisplayName("updateQuestion should throw exception for type mismatch")
    void updateQuestion_whenTypeMismatch_shouldThrowException() {
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(new TrueFalseQuestion()));
        assertThrows(IllegalArgumentException.class, () -> questionService.updateQuestion(QUESTION_ID, new MultipleChoiceQuestionRequestDto()));
    }

    @Test
    @DisplayName("getQuestionById should throw exception when question is not found")
    void getQuestionById_whenQuestionNotFound_shouldThrowException() {
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> questionService.getQuestionById(QUESTION_ID));
    }

    @Test
    @DisplayName("getQuestionById should throw exception for unknown entity type to cover mapEntityToResponseDto else branch")
    void getQuestionById_withUnknownEntityType_shouldThrowException() {
        Question unknownQuestion = mock(Question.class);
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(unknownQuestion));
        assertThrows(IllegalArgumentException.class, () -> questionService.getQuestionById(QUESTION_ID));
    }
}