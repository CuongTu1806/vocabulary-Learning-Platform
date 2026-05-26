package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CardEaseStatResponse {
    private List<CardEaseItemDto> data;
    /** Trung vị hệ số ease SM–2 (~1.3–3.0), VD 2.5 */
    private Double medianEaseFactor;
    /** Trung vị cùng thang với trục phân nhóm histogram: làm tròn(factor × 100) */
    private Integer medianEasePercent;
}
