package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassBoardCommentResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}