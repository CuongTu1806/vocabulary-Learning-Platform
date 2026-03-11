package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "class_invite")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class ClassInviteEntity extends BaseEntity {

    // cần có chuẩn cho cái này
    @Column(name = "status")
    private String status;

    // classroom 1 - N invite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassroomEntity classroom;

    // user 1 - N invite: user (người được mời)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private UserEntity invitedUser;

    // user 1 - N invite : user (người đi mời)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private UserEntity invitedByUser;

}
