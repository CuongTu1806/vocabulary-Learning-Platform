package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

/**
 * Một dòng trên bảng xếp hạng global (tổng điểm contest).
 * {@code rank} có thể null khi trả về thống kê user chưa có submission nào.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    private Integer rank;
    private Long userId;
    private String username;

    private int rating;

    private int contestCount;
}
