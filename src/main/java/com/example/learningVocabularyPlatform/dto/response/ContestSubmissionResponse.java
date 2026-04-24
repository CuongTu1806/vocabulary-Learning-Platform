package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestSubmissionResponse {

    private Long id;
    private Long contestId;
    private Long problemId;
    private Long userId;
    private String userAnswer;
    private int score;
    /** Đúng / sai (theo so khớp đáp án) */
    private Boolean correct;
    private String status;
    private LocalDateTime submittedAt;
}
