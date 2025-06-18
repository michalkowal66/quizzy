package com.example.quizzy.services;

import com.example.quizzy.dto.PlayableChoiceDto;
import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.QuizResultDto;
import com.example.quizzy.dto.QuizSubmissionDto;
import com.example.quizzy.dto.question.*;
import com.example.quizzy.dto.result.GradedMatchingQuestionDto;
import com.example.quizzy.dto.result.GradedMultipleChoiceQuestionDto;
import com.example.quizzy.dto.result.GradedQuestionDto;
import com.example.quizzy.dto.result.GradedTrueFalseQuestionDto;
import com.example.quizzy.dto.submission.AnswerSubmissionDto;
import com.example.quizzy.dto.submission.MatchingAnswerSubmissionDto;
import com.example.quizzy.dto.submission.MultipleChoiceAnswerSubmissionDto;
import com.example.quizzy.dto.submission.TrueFalseAnswerSubmissionDto;
import com.example.quizzy.entity.*;
import com.example.quizzy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service implementation for managing quiz attempts.
 * This includes starting an attempt, submitting answers, and eventually grading them.
 */
@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public QuizAttemptStartResponseDto startQuiz(Long quizId) {
        User currentUser = getCurrentUser();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        // Create and save the attempt record
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(currentUser);
        attempt.setQuiz(quiz);
        attempt.setStartedAt(LocalDateTime.now());
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Map questions to playable DTOs (without correct answers)
        List<PlayableQuestionDto> playableQuestions = quiz.getQuestions().stream()
                .map(this::mapToPlayableDto)
                .collect(Collectors.toList());

        return new QuizAttemptStartResponseDto(savedAttempt.getId(), playableQuestions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public QuizResultDto submitAnswers(Long attemptId, QuizSubmissionDto submissionDto) {
        // Fetch the parent QuizAttempt entity
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found with id: " + attemptId));

        // Validate that the attempt has not been submitted before
        if (attempt.getFinishedAt() != null) {
            throw new IllegalStateException("Quiz attempt has already been submitted.");
        }

        // Map submission DTOs to Answer entities and link them to the parent attempt
        List<Answer> newAnswers = submissionDto.getAnswers().stream()
                .map(this::mapSubmissionDtoToEntity)
                .peek(answer -> answer.setQuizAttempt(attempt)) // Set the owning side of the relationship
                .collect(Collectors.toList());

        // Update the attempt's own list of answers.
        if (attempt.getAnswers() == null) {
            attempt.setAnswers(new ArrayList<>());
        }
        attempt.getAnswers().clear();
        attempt.getAnswers().addAll(newAnswers);

        // Grading logic
        Map<Long, AnswerSubmissionDto> answersMap = submissionDto.getAnswers().stream()
                .collect(Collectors.toMap(AnswerSubmissionDto::getQuestionId, Function.identity()));

        List<GradedQuestionDto> gradedQuestions = new ArrayList<>();
        int correctAnswersCount = 0;
        List<Question> questions = attempt.getQuiz().getQuestions();

        for (Question question : questions) {
            AnswerSubmissionDto submittedAnswerDto = answersMap.get(question.getId());
            GradedQuestionDto gradedDto = gradeQuestion(question, submittedAnswerDto);
            if (gradedDto.isCorrect()) {
                correctAnswersCount++;
            }
            gradedQuestions.add(gradedDto);
        }

        double score = (questions.isEmpty()) ? 0.0 : ((double) correctAnswersCount / questions.size()) * 100;

        // Update the attempt's status and score
        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setScore(score);

        // Save only the parent entity (QuizAttempt).
        // Due to CascadeType.ALL, Hibernate will automatically save all the new Answer entities
        // that were added to the attempt's list.
        quizAttemptRepository.save(attempt);

        return QuizResultDto.builder()
                .attemptId(attemptId)
                .quizTitle(attempt.getQuiz().getTitle())
                .totalQuestions(questions.size())
                .correctAnswersCount(correctAnswersCount)
                .score(score)
                .gradedQuestions(gradedQuestions)
                .build();
    }

    /**
     * Maps a Question entity to its corresponding "playable" DTO, hiding correct answers.
     * @param question The Question entity from the database.
     * @return A DTO safe to be sent to a user taking a quiz.
     */
    private PlayableQuestionDto mapToPlayableDto(Question question) {
        PlayableQuestionDto dto;

        if (question instanceof MultipleChoiceQuestion mcQuestion) {
            PlayableMultipleChoiceQuestionDto mcDto = new PlayableMultipleChoiceQuestionDto();
            mcDto.setChoices(mcQuestion.getChoices().stream()
                    .map(choice -> new PlayableChoiceDto(choice.getId(), choice.getText()))
                    .collect(Collectors.toList()));
            dto = mcDto;
        } else if (question instanceof TrueFalseQuestion) {
            dto = new PlayableTrueFalseQuestionDto();
        } else if (question instanceof MatchingQuestion mQuestion) {
            PlayableMatchingQuestionDto mDto = new PlayableMatchingQuestionDto();
            mDto.setSourceItems(mQuestion.getPairs().stream().map(MatchPair::getSourceItem).collect(Collectors.toList()));

            // Shuffle the target items to present a challenge
            List<String> targetItems = mQuestion.getPairs().stream().map(MatchPair::getTargetItem).collect(Collectors.toList());
            Collections.shuffle(targetItems);
            mDto.setTargetItems(targetItems);

            dto = mDto;
        } else {
            throw new IllegalArgumentException("Unknown question type for mapping: " + question.getId());
        }

        dto.setId(question.getId());
        dto.setText(question.getText());
        return dto;
    }

    /**
     * Maps a submission DTO to a corresponding Answer entity.
     * @param dto The polymorphic answer submission DTO.
     * @return A concrete Answer entity.
     */
    private Answer mapSubmissionDtoToEntity(AnswerSubmissionDto dto) {
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + dto.getQuestionId()));

        Answer answer;
        if (dto instanceof TrueFalseAnswerSubmissionDto tfDto) {
            TrueFalseAnswer tfAnswer = new TrueFalseAnswer();
            tfAnswer.setSubmittedAnswer(tfDto.isSubmittedAnswer());
            answer = tfAnswer;
        } else if (dto instanceof MultipleChoiceAnswerSubmissionDto mcDto) {
            MultipleChoiceAnswer mcAnswer = new MultipleChoiceAnswer();
            Set<Choice> selectedChoices = mcDto.getSelectedChoiceIds().stream()
                    .map(choiceId -> choiceRepository.findById(choiceId)
                            .orElseThrow(() -> new RuntimeException("Choice not found with id: " + choiceId)))
                    .collect(Collectors.toSet());
            mcAnswer.setSubmittedChoices(selectedChoices);
            answer = mcAnswer;
        } else if (dto instanceof MatchingAnswerSubmissionDto mDto) {
            MatchingAnswer mAnswer = new MatchingAnswer();
            List<SubmittedPair> submittedPairs = mDto.getSubmittedPairs().entrySet().stream()
                    .map(entry -> {
                        SubmittedPair pair = new SubmittedPair();
                        pair.setSourceItem(entry.getKey());
                        pair.setTargetItem(entry.getValue());
                        return pair;
                    }).collect(Collectors.toList());
            mAnswer.setSubmittedPairs(submittedPairs);
            answer = mAnswer;
        } else {
            throw new IllegalArgumentException("Unknown answer submission type");
        }

        answer.setQuestion(question);
        return answer;
    }

    /**
     * Private helper method to grade a single question.
     * It compares the user's submitted answer with the correct answer from the database entity.
     *
     * @param question The original Question entity with the correct answer.
     * @param submittedAnswerDto The user's submitted answer DTO.
     * @return A DTO containing the detailed result for this specific question.
     */
    private GradedQuestionDto gradeQuestion(Question question, AnswerSubmissionDto submittedAnswerDto) {
        // This variable will hold the specific DTO, but is declared as the base type.
        GradedQuestionDto gradedDto;

        if (submittedAnswerDto == null) {
            // Handle case where an answer was not submitted for a question.
            throw new IllegalArgumentException("No answer submitted for question ID: " + question.getId());
        }

        if (question instanceof TrueFalseQuestion tfQuestion && submittedAnswerDto instanceof TrueFalseAnswerSubmissionDto tfAnswer) {
            GradedTrueFalseQuestionDto specificDto = new GradedTrueFalseQuestionDto();
            specificDto.setCorrectAnswer(tfQuestion.isCorrectAnswer());
            specificDto.setSubmittedAnswer(tfAnswer.isSubmittedAnswer());
            specificDto.setCorrect(tfQuestion.isCorrectAnswer() == tfAnswer.isSubmittedAnswer());
            gradedDto = specificDto; // Assign the specific DTO to the base DTO variable

        } else if (question instanceof MultipleChoiceQuestion mcQuestion && submittedAnswerDto instanceof MultipleChoiceAnswerSubmissionDto mcAnswer) {
            GradedMultipleChoiceQuestionDto specificDto = new GradedMultipleChoiceQuestionDto();
            Set<Long> correctChoiceIds = mcQuestion.getChoices().stream()
                    .filter(Choice::isCorrect)
                    .map(Choice::getId)
                    .collect(Collectors.toSet());

            specificDto.setCorrectChoices(mcQuestion.getChoices().stream().filter(Choice::isCorrect).map(Choice::getText).collect(Collectors.toSet()));
            specificDto.setSubmittedChoices(choiceRepository.findAllById(mcAnswer.getSelectedChoiceIds()).stream().map(Choice::getText).collect(Collectors.toSet()));
            specificDto.setCorrect(correctChoiceIds.equals(mcAnswer.getSelectedChoiceIds()));
            gradedDto = specificDto; // Assign to the base DTO variable

        } else if (question instanceof MatchingQuestion mQuestion && submittedAnswerDto instanceof MatchingAnswerSubmissionDto mAnswer) {
            GradedMatchingQuestionDto specificDto = new GradedMatchingQuestionDto();
            Map<String, String> correctPairs = mQuestion.getPairs().stream()
                    .collect(Collectors.toMap(MatchPair::getSourceItem, MatchPair::getTargetItem));

            specificDto.setCorrectPairs(correctPairs);
            specificDto.setSubmittedPairs(mAnswer.getSubmittedPairs());
            specificDto.setCorrect(correctPairs.equals(mAnswer.getSubmittedPairs()));
            gradedDto = specificDto; // Assign to the base DTO variable

        } else {
            throw new IllegalArgumentException("Cannot grade question type mismatch for question ID: " + question.getId());
        }

        // Set the common base fields after the specific DTO has been created and populated.
        gradedDto.setQuestionId(question.getId());
        gradedDto.setText(question.getText());

        return gradedDto;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User entity.
     * @throws UsernameNotFoundException if the user cannot be found in the repository.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
