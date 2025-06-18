package com.example.quizzy.security;

import com.example.quizzy.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("quizAttemptSecurityService")
@RequiredArgsConstructor
public class QuizAttemptSecurityService {

    private final QuizAttemptRepository quizAttemptRepository;

    public boolean isOwner(Long attemptId, Authentication authentication) {
        String currentUsername = authentication.getName();
        return quizAttemptRepository.findById(attemptId)
                .map(attempt -> attempt.getUser().getUsername().equals(currentUsername))
                .orElse(false);
    }
}
