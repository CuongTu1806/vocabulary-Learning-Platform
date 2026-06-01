package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestRankingResponse {

    private int rank;

    private Long userId;
    private String username;
    private int totalScore;
}
