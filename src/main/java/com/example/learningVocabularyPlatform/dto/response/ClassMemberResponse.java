package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMemberResponse {
    private Long id;
    private Long classId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
}
