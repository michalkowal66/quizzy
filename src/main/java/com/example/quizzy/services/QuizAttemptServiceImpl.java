package com.example.quizzy.services;

import com.example.quizzy.dto.PlayableChoiceDto;
import com.example.quizzy.dto.QuizAttemptStartResponseDto;
import com.example.quizzy.dto.question.*;
import com.example.quizzy.entity.*;
import com.example.quizzy.repository.QuizAttemptRepository;
import com.example.quizzy.repository.QuizRepository;
import com.example.quizzy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;

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

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
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
}
