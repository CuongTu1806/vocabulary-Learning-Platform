package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LessonResponse {
    private Long id;
    private String title;
    private String description;
    private int numberOfWords;
    private Long ownerId;
    private String ownerUsername;
    private String visibility;
    private Integer downloadCount;
    private Boolean currentUserCanQuiz;
    private LocalDateTime createdAt;
}
