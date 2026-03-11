package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "classroom")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor

public class ClassroomEntity extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    // User 1 - N classroom (sở hữu, chủ/ owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    // Classroom 1 - N Class_member
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<ClassMemberEntity> classMembers;

    // Classroom 1 - N Class Lesson
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<ClassLessonEntity> classLessons;

    // Classroom 1 - N Assignment
    @OneToMany(mappedBy ="classroom", fetch = FetchType.LAZY)
    private List<AssignmentEntity> assignments;

    // classroom 1 - N invite
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<ClassInviteEntity> classInvites;
}
