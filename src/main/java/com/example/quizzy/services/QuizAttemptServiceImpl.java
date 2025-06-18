package com.example.quizzy.services;

import com.example.quizzy.dto.PlayableChoiceDto;
import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.QuizSubmissionDto;
import com.example.quizzy.dto.question.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public void submitAnswers(Long attemptId, QuizSubmissionDto submissionDto) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found with id: " + attemptId));

        // Validate that the attempt has not been submitted before
        if (attempt.getFinishedAt() != null) {
            throw new IllegalStateException("Quiz attempt has already been submitted.");
        }

        // Map submission DTOs to Answer entities and associate them with the attempt
        List<Answer> answers = submissionDto.getAnswers().stream()
                .map(this::mapSubmissionDtoToEntity)
                .peek(answer -> answer.setQuizAttempt(attempt))
                .collect(Collectors.toList());

        answerRepository.saveAll(answers);

        // Mark the attempt as finished
        attempt.setFinishedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt);
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
