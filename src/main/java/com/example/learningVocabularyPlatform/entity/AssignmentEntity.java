package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class AssignmentEntity extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // Class 1 - N Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassroomEntity classroom;

    // User 1 - N Assignment (1 user giao bai tap / giao vien/ lop truong)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity userCreated;

    // Assignment 1 - N assignment_submission
    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    private List<AssignmentSubmissionEntity> assignmentSubmissions;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AssignmentAttachmentEntity> attachments;
}
