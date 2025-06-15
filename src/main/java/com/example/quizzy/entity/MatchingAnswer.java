package com.example.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "matching_answers")
public class MatchingAnswer extends Answer {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "answer_id")
    private List<SubmittedPair> submittedPairs;
}
