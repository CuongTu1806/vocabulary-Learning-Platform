package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AddChartDataDto {
    private String day;
    private Long added;  // số từ được thêm mới
}
