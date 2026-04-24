package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class DashboardResponse {
    private int lessons;
    private int words;
    private int streak;
}
