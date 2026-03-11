package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class NotificationEntity extends BaseEntity {

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    // loại thông báo, vd invite/ assignment ...  kèm theo id bên dưới
    @Column(name = "type")
    private String type;

    // id tương ứng với type, ví dụ thông báo lớp: type : classroom -> id = 10 là id classroom
    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(name  = "is_read", columnDefinition = "False")
    private Boolean isRead;

    // user 1 - N notification (1 user nhận nhiều thông báo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
