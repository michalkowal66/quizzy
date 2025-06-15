package com.example.quizzy.security;

import com.example.quizzy.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("quizSecurityService") // for @PreAuthorize
@RequiredArgsConstructor
public class QuizSecurityService {

    private final QuizRepository quizRepository;

    /**
     * Checks if the authenticated user is the owner of a specific quiz.
     * @param quizId The ID of the quiz to check.
     * @param authentication The authentication object for the current user.
     * @return true if the user is the owner, false otherwise.
     */
    public boolean isOwner(Long quizId, Authentication authentication) {
        String currentUsername = authentication.getName();
        return quizRepository.findById(quizId)
                .map(quiz -> quiz.getAuthor().getUsername().equals(currentUsername))
                .orElse(false);
    }
}
