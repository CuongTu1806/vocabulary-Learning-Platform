package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_schedule")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor

public class ReviewScheduleEntity extends BaseEntity {

    @Column(name = "repetation_level")
    private int repetationLevel;

    @Column(name = "state", length = 20)
    private String state;

    @Column(name = "learning_step")
    private int learningStep;

    @Column(name = "interval_days")
    private int intervalDays;

    @Column(name = "ease_factor")
    private double easeFactor;

    @Column(name = "delay_factor")
    private double delayFactor;

    @Column(name = "next_review_date")
    private LocalDateTime nextReviewDate;

    @Column(name = "last_review_date")
    private LocalDateTime lastReviewDate;

    // user_vocabulary 1 - N review_schedule
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id")
    private UserVocabularyEntity userVocabulary;
}
