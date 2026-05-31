package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDetailResponse {
    private Integer questionIndex;
    private String questionText;
    private String studentAnswer;
    private String expectedAnswer;
    private Boolean correct;
}
