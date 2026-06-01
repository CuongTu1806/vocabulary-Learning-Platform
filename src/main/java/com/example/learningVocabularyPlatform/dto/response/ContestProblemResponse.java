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
    /** Ảnh minh họa khi làm bài (URL ngoài), nếu có. */
    private String imageUrl;
    /** Có ảnh upload trên server — client gọi GET .../image (kèm JWT qua axios blob). */
    private Boolean hasUploadedImage;
    /** Không trả wrongAnswer / answer đúng — tránh lộ đề. */
    private String difficulty;
    private Integer maxScore;
    private Integer orderIndex;
}
