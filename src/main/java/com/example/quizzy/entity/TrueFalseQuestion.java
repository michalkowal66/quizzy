package com.example.quizzy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "true_false_questions")
public class TrueFalseQuestion extends Question {
    @Column(nullable = false)
    private boolean correctAnswer;
}