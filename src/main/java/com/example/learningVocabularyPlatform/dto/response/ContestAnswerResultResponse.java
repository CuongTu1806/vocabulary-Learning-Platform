package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

/** Kết quả sau khi nộp một câu — dùng cho hiệu ứng đúng/sai + cập nhật điểm. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestAnswerResultResponse {
    private Long problemId;
    private boolean correct;
    /** Điểm nhận được cho câu này */
    private int scoreAwarded;
    private int maxScoreForProblem;
    /** Tổng điểm của user trong contest sau câu này */
    private int totalScore;
}
