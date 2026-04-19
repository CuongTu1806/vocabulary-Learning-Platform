package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestProblemResponse {

    private Long id;
    private String title;
    private String description;
    private String wrongAnswer;
    private String difficulty;
    private Integer maxScore;
    private Integer orderIndex;
}
