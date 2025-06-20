package com.example.quizzy.repository;

import com.example.quizzy.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByAuthorId(Long userId);
    List<Quiz> findByTitleContainingIgnoreCase(String title);
}