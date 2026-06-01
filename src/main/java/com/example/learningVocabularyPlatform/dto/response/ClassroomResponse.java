package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime createdAt;
    /** Gắn khi có user đăng nhập: có phải chủ lớp không */
    private Boolean currentUserIsOwner;
}
