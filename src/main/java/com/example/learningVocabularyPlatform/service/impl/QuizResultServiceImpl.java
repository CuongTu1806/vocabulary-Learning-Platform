package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.response.QuizResultDetailResponse;
import com.example.learningVocabularyPlatform.entity.QuizResultEntity;
import com.example.learningVocabularyPlatform.mapper.QuizResultMapper;
import com.example.learningVocabularyPlatform.repository.QuizResultRepository;
import com.example.learningVocabularyPlatform.service.QuizResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizResultServiceImpl implements QuizResultService {
    private final QuizResultRepository quizResultRepository;
    private final QuizResultMapper quizResultMapper;

    @Override
    public List<QuizResultDetailResponse> getDetail(Long quizId) {
        List<QuizResultEntity> quizResultEntityList = quizResultRepository.findByQuiz_Id(quizId);
        List<QuizResultDetailResponse> result = new ArrayList<>();
        for (QuizResultEntity quizResultEntity : quizResultEntityList) {
            result.add(quizResultMapper.entityToResponse(quizResultEntity));
        }
        return result;
    }
}
