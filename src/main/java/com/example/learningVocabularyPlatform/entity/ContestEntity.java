package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contest")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class ContestEntity extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // ai được tham gia/ chưa cần lắm
    @Column(name = "visibility")
    private String visibility;

    // 1 user - N contest (tạo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity userCreated;

    // contest 1 -N contest_participant
    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY)
    private List<ContestParticipantEntity> contestParticipants;

    // contest 1 - N contestProblem
    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY)
    private List<ContestProblemEntity> contestProblems;

    // contest 1 - N contest submit
    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY)
    private List<ContestSubmissionEntity> contestSubmissions;
}
