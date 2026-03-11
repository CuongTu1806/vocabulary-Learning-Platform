package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;

    // User 1 - N Lesson
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<LessonEntity> lessons;

    // User 1 - N user_vocabulary
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserVocabularyEntity> userVocabularies;

    // user 1 - N Quiz
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<QuizEntity> quizzes;

    // User 1 - N classroom (chủ lớp, owner)
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<ClassroomEntity> classrooms;

    // User 1 - N Class_member
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ClassMemberEntity> classMembers;

    // User 1 - N  assignment (1 nguoi giao bai tap/ giao vien/ lop truong)
    @OneToMany(mappedBy = "userCreated")
    private List<AssignmentEntity> assignments;

    // user 1 - N Assignment_submission (1 nguoi lam nhieu bai)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AssignmentSubmissionEntity> assignmentSubmissions;

    // user 1 - N Notification (1 user nhận nhiều thông báo)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<NotificationEntity> notifications;

    // user 1 - N invite (người được mời)
    @OneToMany(mappedBy = "invitedUser", fetch = FetchType.LAZY)
    private List<ClassInviteEntity> classesWasInvited;

    // user 1 - N invite (người đi mời)
    @OneToMany(mappedBy = "invitedByUser", fetch = FetchType.LAZY)
    private List<ClassInviteEntity> classInvites;

    // User 1 - N Contest (tạo)
    @OneToMany(mappedBy = "userCreated", fetch = FetchType.LAZY)
    private List<ContestEntity> contests;

    // User 1 - N contest_participant
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ContestParticipantEntity> contestParticipants;

    //user 1 - N contest submit
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ContestSubmissionEntity> contestSubmissions;

    // user 1 - 1 leaderboard
    @OneToOne(mappedBy = "user")
    private ServerLeaderboardEntity serverLeaderboard;
}

