package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assignment_submission_detail")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionDetailEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private AssignmentSubmissionEntity submission;

    @Column(name = "question_index")
    private Integer questionIndex;

    @Lob
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Lob
    @Column(name = "student_answer", columnDefinition = "TEXT")
    private String studentAnswer;

    @Lob
    @Column(name = "expected_answer", columnDefinition = "TEXT")
    private String expectedAnswer;

    @Column(name = "correct_flag")
    private Boolean correct;
}
