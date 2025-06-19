package com.example.quizzy.services;

import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.QuizResultDto;
import com.example.quizzy.dto.QuizSubmissionDto;
import com.example.quizzy.dto.question.PlayableMatchingQuestionDto;
import com.example.quizzy.dto.question.PlayableMultipleChoiceQuestionDto;
import com.example.quizzy.dto.submission.MatchingAnswerSubmissionDto;
import com.example.quizzy.dto.submission.MultipleChoiceAnswerSubmissionDto;
import com.example.quizzy.dto.submission.TrueFalseAnswerSubmissionDto;
import com.example.quizzy.entity.*;
import com.example.quizzy.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Quiz Attempt Service Implementation Tests")
class QuizAttemptServiceImplTest {

    // Mocks for all repository dependencies
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private ChoiceRepository choiceRepository;

    // The service instance to be tested, with mocks injected
    @InjectMocks
    private QuizAttemptServiceImpl quizAttemptService;

    private User testUser;
    private Quiz testQuiz;
    private TrueFalseQuestion tfQuestion;
    private MultipleChoiceQuestion mcQuestion;
    private MatchingQuestion matchingQuestion;


    @BeforeEach
    void setUp() {
        // Prepare common test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Create one of each question type
        tfQuestion = new TrueFalseQuestion();
        tfQuestion.setId(10L);
        tfQuestion.setText("Is this a test?");
        tfQuestion.setCorrectAnswer(true);

        mcQuestion = createTestMCQuestion();
        matchingQuestion = createTestMatchingQuestion();

        testQuiz = new Quiz();
        testQuiz.setId(1L);
        testQuiz.setTitle("Test Quiz");
        testQuiz.setQuestions(Arrays.asList(tfQuestion, mcQuestion, matchingQuestion));
        tfQuestion.setQuiz(testQuiz);
        mcQuestion.setQuiz(testQuiz);
        matchingQuestion.setQuiz(testQuiz);
    }

    // Helper methods to create complex entities
    private MultipleChoiceQuestion createTestMCQuestion() {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion();
        question.setId(20L);
        question.setText("Which are JVM languages?");
        Choice choice1 = new Choice();
        choice1.setId(1L);
        choice1.setText("Java");
        choice1.setCorrect(true);
        Choice choice2 = new Choice();
        choice2.setId(2L);
        choice2.setText("Kotlin");
        choice2.setCorrect(true);
        Choice choice3 = new Choice();
        choice3.setId(3L);
        choice3.setText("TypeScript");
        choice3.setCorrect(false);
        question.setChoices(Arrays.asList(choice1, choice2, choice3));
        return question;
    }

    private MatchingQuestion createTestMatchingQuestion() {
        MatchingQuestion question = new MatchingQuestion();
        question.setId(30L);
        question.setText("Match the capital to the country.");
        MatchPair pair1 = new MatchPair(1L, "Poland", "Warsaw");
        MatchPair pair2 = new MatchPair(2L, "Germany", "Berlin");
        question.setPairs(Arrays.asList(pair1, pair2));
        return question;
    }


    @Test
    @DisplayName("startQuiz should throw exception when quiz not found")
    void startQuiz_whenQuizNotFound_shouldThrowException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> quizAttemptService.startQuiz(1L));
        }
    }

    @Test
    @DisplayName("startQuiz with all question types should map all to playable DTOs")
    void startQuiz_withAllQuestionTypes_shouldMapAllToPlayableDtos() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
            when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
                QuizAttempt attempt = inv.getArgument(0);
                attempt.setId(100L);
                return attempt;
            });

            // Act
            QuizAttemptStartResponseDto response = quizAttemptService.startQuiz(1L);

            // Assert
            assertEquals(3, response.getQuestions().size());
            assertTrue(response.getQuestions().get(0) instanceof com.example.quizzy.dto.question.PlayableTrueFalseQuestionDto);
            assertTrue(response.getQuestions().get(1) instanceof PlayableMultipleChoiceQuestionDto);
            assertTrue(response.getQuestions().get(2) instanceof PlayableMatchingQuestionDto);

            PlayableMatchingQuestionDto matchingDto = (PlayableMatchingQuestionDto) response.getQuestions().get(2);
            assertEquals(2, matchingDto.getSourceItems().size());
            assertEquals(2, matchingDto.getTargetItems().size());
            // Target items should be shuffled, so they are not guaranteed to be in the original order
            assertTrue(matchingDto.getTargetItems().contains("Warsaw"));
            assertTrue(matchingDto.getTargetItems().contains("Berlin"));
        }
    }

    @Test
    @DisplayName("submitAnswers with all question types (all correct) should score 100%")
    void submitAnswers_withAllQuestionTypesAndAllCorrect_shouldScore100() {
        // Arrange
        QuizAttempt attempt = new QuizAttempt(100L, testUser, testQuiz, null, null, null, new ArrayList<>());
        when(quizAttemptRepository.findById(100L)).thenReturn(Optional.of(attempt));

        // Mock repository calls needed for mapping and grading
        when(questionRepository.findById(10L)).thenReturn(Optional.of(tfQuestion));
        when(questionRepository.findById(20L)).thenReturn(Optional.of(mcQuestion));
        when(questionRepository.findById(30L)).thenReturn(Optional.of(matchingQuestion));
        when(choiceRepository.findById(1L)).thenReturn(Optional.of(mcQuestion.getChoices().get(0)));
        when(choiceRepository.findById(2L)).thenReturn(Optional.of(mcQuestion.getChoices().get(1)));

        // Prepare a submission with all correct answers
        QuizSubmissionDto submissionDto = new QuizSubmissionDto();
        TrueFalseAnswerSubmissionDto tfAnswer = new TrueFalseAnswerSubmissionDto();
        tfAnswer.setQuestionId(10L);
        tfAnswer.setSubmittedAnswer(true);

        MultipleChoiceAnswerSubmissionDto mcAnswer = new MultipleChoiceAnswerSubmissionDto();
        mcAnswer.setQuestionId(20L);
        mcAnswer.setSelectedChoiceIds(Set.of(1L, 2L));

        MatchingAnswerSubmissionDto matchingAnswer = new MatchingAnswerSubmissionDto();
        matchingAnswer.setQuestionId(30L);
        matchingAnswer.setSubmittedPairs(Map.of("Poland", "Warsaw", "Germany", "Berlin"));

        submissionDto.setAnswers(Arrays.asList(tfAnswer, mcAnswer, matchingAnswer));

        // Act
        QuizResultDto result = quizAttemptService.submitAnswers(100L, submissionDto);

        // Assert
        assertEquals(100.0, result.getScore());
        assertEquals(3, result.getCorrectAnswersCount());
        assertTrue(result.getGradedQuestions().stream().allMatch(gq -> gq.isCorrect()));

        // Verify save was called with correct state
        ArgumentCaptor<QuizAttempt> attemptCaptor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(attemptCaptor.capture());
        assertEquals(100.0, attemptCaptor.getValue().getScore());
        assertNotNull(attemptCaptor.getValue().getFinishedAt());
    }

    @Test
    @DisplayName("submitAnswers with incorrect answers should calculate score correctly")
    void submitAnswers_withIncorrectAnswers_shouldCalculateScore() {
        // Arrange
        QuizAttempt attempt = new QuizAttempt(100L, testUser, testQuiz, null, null, null, new ArrayList<>());
        when(quizAttemptRepository.findById(100L)).thenReturn(Optional.of(attempt));

        // Mock repository calls
        when(questionRepository.findById(10L)).thenReturn(Optional.of(tfQuestion));
        when(questionRepository.findById(20L)).thenReturn(Optional.of(mcQuestion));
        when(questionRepository.findById(30L)).thenReturn(Optional.of(matchingQuestion));
        when(choiceRepository.findById(1L)).thenReturn(Optional.of(mcQuestion.getChoices().get(0)));

        // Prepare a submission with 1 correct and 2 incorrect answers
        QuizSubmissionDto submissionDto = new QuizSubmissionDto();
        TrueFalseAnswerSubmissionDto tfAnswer = new TrueFalseAnswerSubmissionDto();
        tfAnswer.setQuestionId(10L);
        tfAnswer.setSubmittedAnswer(true); // Correct

        MultipleChoiceAnswerSubmissionDto mcAnswer = new MultipleChoiceAnswerSubmissionDto();
        mcAnswer.setQuestionId(20L);
        mcAnswer.setSelectedChoiceIds(Set.of(1L)); // Incorrect (missing one)

        MatchingAnswerSubmissionDto matchingAnswer = new MatchingAnswerSubmissionDto();
        matchingAnswer.setQuestionId(30L);
        matchingAnswer.setSubmittedPairs(Map.of("Poland", "Berlin", "Germany", "Warsaw")); // Incorrect

        submissionDto.setAnswers(Arrays.asList(tfAnswer, mcAnswer, matchingAnswer));

        // Act
        QuizResultDto result = quizAttemptService.submitAnswers(100L, submissionDto);

        // Assert
        assertEquals(1, result.getCorrectAnswersCount());
        // Score should be (1.0 / 3.0) * 100
        assertEquals(33.33, result.getScore(), 0.01);
        assertTrue(result.getGradedQuestions().get(0).isCorrect());
        assertFalse(result.getGradedQuestions().get(1).isCorrect());
        assertFalse(result.getGradedQuestions().get(2).isCorrect());
    }


    @Test
    @DisplayName("submitAnswers should throw exception when attempt is already finished")
    void submitAnswers_whenAlreadySubmitted_shouldThrowException() {
        // Arrange
        QuizAttempt finishedAttempt = new QuizAttempt();
        finishedAttempt.setFinishedAt(java.time.LocalDateTime.now().minusHours(1));
        when(quizAttemptRepository.findById(100L)).thenReturn(Optional.of(finishedAttempt));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> quizAttemptService.submitAnswers(100L, new QuizSubmissionDto()));
    }
}