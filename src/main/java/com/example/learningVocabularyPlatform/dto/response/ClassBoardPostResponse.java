package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassBoardPostResponse {
    private Long id;
    private Long classroomId;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private List<ClassBoardCommentResponse> comments;
    private Integer commentCount;
}