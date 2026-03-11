package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "contest_problem")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class ContestProblemEntity extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    // lưu dãy đáp án nhiễu vd: abc/def/12dv
    @Column(name = "wrong_answer")
    private String wrongAnswer;

    @Column(name = "answer")
    private String answer;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "order_index")
    private Integer orderIndex;

    // contest 1 - N contest_problem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private ContestEntity contest;

    // problem 1 - N contest submit
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    private List<ContestSubmissionEntity> contestSubmissions;

}
