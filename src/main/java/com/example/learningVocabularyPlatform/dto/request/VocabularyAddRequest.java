package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class VocabularyAddRequest {
    private String word;
    private String pronunciation;
    private String pos;
    private String audio_path;
    private String image_path;
    private String meaning;
    private String example;
}
