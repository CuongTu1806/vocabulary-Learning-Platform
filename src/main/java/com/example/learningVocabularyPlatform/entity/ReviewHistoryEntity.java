package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review_history")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewHistoryEntity extends BaseEntity {

    @Column(name = "rating", length = 20)
    private String rating;

    @Column(name = "old_ease_factor")
    private double oldEaseFactor;

    @Column(name = "new_ease_factor")
    private double newEaseFactor;

    @Column(name = "old_interval_days")
    private int oldIntervalDays;

    @Column(name = "new_interval_days")
    private int newIntervalDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vocabulary_id")
    private UserVocabularyEntity userVocabulary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_schedule_id")
    private ReviewScheduleEntity reviewSchedule;
}
