package com.example.quizzy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "true_false_answers")
public class TrueFalseAnswer extends Answer {
    @Column(nullable = false)
    private boolean submittedAnswer;
}
