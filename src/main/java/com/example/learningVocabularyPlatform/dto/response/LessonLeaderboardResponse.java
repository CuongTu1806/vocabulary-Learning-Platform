package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonLeaderboardResponse {

    private Integer rank;
    private Long lessonId;
    private String title;
    private Long ownerId;
    private String ownerUsername;
    private int downloadCount;
}