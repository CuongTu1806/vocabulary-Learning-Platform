package com.example.learningVocabularyPlatform.mapper;

import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonMapper {
    private final UserVocabularyRepository userVocabularyRepository;

    public LessonResponse convertLessonToResponse(LessonEntity ls) {
        int numberOfWords = userVocabularyRepository.countByLesson_Id(ls.getId());
        return LessonResponse.builder()
                .id(ls.getId())
                .title(ls.getTitle())
                .createdAt(ls.getCreatedAt())
                .description(ls.getDescription())
                .numberOfWords(numberOfWords)
                .build();
    }

    public LessonEntity convertRequestToLessonEntity(LessonRequest request) {
        return LessonEntity.builder()
                .description(request.getDescription())
                .title(request.getTitle())
                .build();
    }
}
