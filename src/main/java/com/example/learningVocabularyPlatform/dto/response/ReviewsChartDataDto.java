package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReviewsChartDataDto {
    private String day;
    private Long learning;
    private Long relearning;
    private Long young;
    private Long mature;
    private Long filtered;
}
