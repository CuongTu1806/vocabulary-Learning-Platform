package com.example.learningVocabularyPlatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "vocabulary_stat")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor

public class VocabularyStatEntity extends BaseEntity {
    @Column(name = "total_review")
    private int totalReview;

    @Column(name = "total_correct")
    private int totalCorrect;

    @Column(name = "total_wrong")
    private int totalWrong;

    @Column(name = "accuracy")
    private double accuracy;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    // User_vocabulary 1 - N vocabulary_stat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id")
    private UserVocabularyEntity userVocabulary;
}
