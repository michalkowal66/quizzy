package com.example.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "match_pairs")
@Getter
@Setter
@NoArgsConstructor
public class MatchPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "left_text")
    private String leftText;

    @Column(name = "right_text")
    private String rightText;
}
