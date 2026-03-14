package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UserVocabularyResponse {
    private Long id;
    private String word;
    private String pos;
    private String pronunciation;
    private String meaning;
    private String example;
    private Long lessonId;
    private String audio_path;
    private String image_path;
    private String type; // user or system
}
