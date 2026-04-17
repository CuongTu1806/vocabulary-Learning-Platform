package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review_setting")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettingEntity extends BaseEntity {

    @Column(name = "learning_steps", length = 200, nullable = false)
    private String learningSteps;

    @Column(name = "max_interval_days", nullable = false)
    private int maxIntervalDays;

    @Column(name = "easy_bonus", nullable = false)
    private double easyBonus;

    @Column(name = "delay_factor", nullable = false)
    private double delayFactor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
