package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.util.List;

/** Thông tin người chơi trong contest: điểm, hạng, tiến độ — cho UI góc màn hình. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestMyStatsResponse {
    private Long userId;
    private String username;
    /** Tổng điểm đã có */
    private int totalScore;
    /** Hạng (1-based), null nếu chưa có điểm / chưa xếp */
    private Integer rank;
    /** Số câu đã trả lời */
    private int problemsAnswered;
    /** Tổng số câu trong đề */
    private int totalProblems;
    /** Id các câu đã nộp — để khôi phục UI sau F5 */
    private List<Long> solvedProblemIds;
}
