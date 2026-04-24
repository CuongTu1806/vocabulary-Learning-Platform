package com.example.learningVocabularyPlatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Một câu hỏi trong contest — dùng khi tạo contest (nhúng trong {@link ContestRequest}).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestProblemRequest {

    @NotBlank
    private String title;

    private String description;

    private String wrongAnswer;

    /** Ảnh minh họa (URL), tuỳ chọn */
    private String imageUrl;

    @NotBlank
    private String answer;

    private String difficulty;

    @NotNull
    private Integer maxScore;

    @NotNull
    private Integer orderIndex;
}
