package com.example.quizzy.repository;

import com.example.quizzy.entity.MatchPair;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPairRepository extends JpaRepository<MatchPair, Long> {
}
