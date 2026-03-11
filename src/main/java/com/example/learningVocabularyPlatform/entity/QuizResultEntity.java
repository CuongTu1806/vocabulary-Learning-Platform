package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "quiz_result")
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor

public class QuizResultEntity extends BaseEntity {

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "true_answer")
    private String trueAnswer;

    @Column(name = "is_corect")
    private boolean isCorrect;

    // Quiz 1 - N quiz_result
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private QuizEntity quiz;

    // user_vocabulary 1 - N quiz_result
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id")
    private UserVocabularyEntity userVocabulary;

}