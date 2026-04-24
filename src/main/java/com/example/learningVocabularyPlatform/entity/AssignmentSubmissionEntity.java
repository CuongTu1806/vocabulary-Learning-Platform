package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment_submission")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class AssignmentSubmissionEntity extends BaseEntity {

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "score")
    private float score;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // assignment 1 - N assigment_submission
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private AssignmentEntity assignment;

    // User 1 - N assignment_submission
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubmissionAttachmentEntity> attachments;

}
