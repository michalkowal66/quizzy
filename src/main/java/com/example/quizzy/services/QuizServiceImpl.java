package com.example.quizzy.services;

import com.example.quizzy.dto.QuizDto;
import com.example.quizzy.dto.QuizResponseDto;
import com.example.quizzy.entity.Quiz;
import com.example.quizzy.entity.User;
import com.example.quizzy.repository.QuizRepository;
import com.example.quizzy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Override
    public QuizResponseDto createQuiz(QuizDto quizDto) {
        User currentUser = getCurrentUser();

        Quiz quiz = new Quiz();
        quiz.setTitle(quizDto.getTitle());
        quiz.setDescription(quizDto.getDescription());
        quiz.setAuthor(currentUser);

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizResponseDto(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponseDto getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
        return mapToQuizResponseDto(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponseDto> getCurrentUserQuizzes() {
        User currentUser = getCurrentUser();
        return quizRepository.findByAuthorId(currentUser.getId()).stream()
                .map(this::mapToQuizResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public QuizResponseDto updateQuiz(Long quizId, QuizDto quizDto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        // Ownership check will be handled by @PreAuthorize in the controller
        quiz.setTitle(quizDto.getTitle());
        quiz.setDescription(quizDto.getDescription());

        Quiz updatedQuiz = quizRepository.save(quiz);
        return mapToQuizResponseDto(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }
        // Ownership check will be handled by @PreAuthorize in the controller
        quizRepository.deleteById(quizId);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * @return The authenticated User entity.
     * @throws UsernameNotFoundException if the user cannot be found.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Maps a Quiz entity to a QuizResponseDto.
     * @param quiz The Quiz entity to map.
     * @return The corresponding QuizResponseDto.
     */
    private QuizResponseDto mapToQuizResponseDto(Quiz quiz) {
        QuizResponseDto dto = new QuizResponseDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setAuthorUsername(quiz.getAuthor().getUsername());
        return dto;
    }
}
