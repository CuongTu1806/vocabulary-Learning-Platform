package com.example.learningVocabularyPlatform.mapper;

import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonResponse convertLessonToResponse(LessonEntity ls) {
        return LessonResponse.builder()
                .id(ls.getId())
                .title(ls.getTitle())
                .createdAt(ls.getCreatedAt())
                .description(ls.getDescription())
                .build();
    }

    public LessonEntity convertRequestToLessonEntity(LessonRequest request) {
        return LessonEntity.builder()
                .description(request.getDescription())
                .title(request.getTitle())
                .build();
    }
}
