package com.example.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "multiple_choice_answers")
public class MultipleChoiceAnswer extends Answer {
    @ManyToMany
    @JoinTable(
            name = "answer_choices",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "choice_id")
    )
    private Set<Choice> submittedChoices;
}
