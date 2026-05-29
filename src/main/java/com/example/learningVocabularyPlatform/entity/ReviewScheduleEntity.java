package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "review_schedule",
    indexes = {
        @Index(name = "idx_review_schedule_user_id", columnList = "user_id"),
        @Index(name = "idx_review_schedule_user_next_review", columnList = "user_id,next_review_date"),
        @Index(name = "idx_review_schedule_user_last_review", columnList = "user_id,last_review_date")
    }
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor

public class ReviewScheduleEntity extends BaseEntity {

    @Column(name = "previous_interval_days")
    private Integer previousIntervalDays;

    @Column(name = "previous_ease_factor")
    private Double previousEaseFactor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // user_vocabulary 1 - N review_schedule
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id")
    private UserVocabularyEntity userVocabulary;
}
