package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest_submission")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor


public class ContestSubmissionEntity extends BaseEntity {

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "score")
    private int score;

    @Column(name = "status")
    private String status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    //contest 1 - N contest_submit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private ContestEntity contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private ContestProblemEntity problem;
}
