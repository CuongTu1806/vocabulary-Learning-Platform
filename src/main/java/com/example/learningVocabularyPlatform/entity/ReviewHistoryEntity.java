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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_schedule_id")
    private ReviewScheduleEntity reviewSchedule;
    // duration of the review action in seconds (optional)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
