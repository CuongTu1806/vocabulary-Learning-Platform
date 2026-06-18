package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import com.example.learningVocabularyPlatform.mapper.VocabularyMapper;
import com.example.learningVocabularyPlatform.repository.LessonRepository;
import com.example.learningVocabularyPlatform.repository.ReviewHistoryRepository;
import com.example.learningVocabularyPlatform.repository.ReviewScheduleRepository;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import com.example.learningVocabularyPlatform.repository.VocabularyRepository;
import com.example.learningVocabularyPlatform.service.UserVocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserVocabularyServiceImpl implements UserVocabularyService {

    private static final int SEARCH_PAGE_SIZE = 30;
    private static final String LESSON_NOT_FOUND = "Lesson not found";
    private static final String LESSON_NOT_OWNED = "Ban khong co quyen sua xoa bai hoc nay";
    private static final String USER_VOCAB_NOT_FOUND = "User Vocabulary not found";

    private final LessonRepository lessonRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final VocabularyMapper vocabularyMapper;
    private final VocabularyRepository vocabularyRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserVocabularyResponse> searchVocabulary(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String q = keyword.trim();
        var page = PageRequest.of(0, SEARCH_PAGE_SIZE);

        List<VocabularyEntity> vc =
                vocabularyRepository.searchByWordOrMeaningContaining(q, page);
        List<UserVocabularyEntity> uvc =
                userVocabularyRepository.searchByWordOrMeaningContaining(q, page);

        List<UserVocabularyResponse> responses = new ArrayList<>();
        for (VocabularyEntity v : vc) {
            responses.add(vocabularyMapper.convertVocabularyToResponse(v));
        }
        for (UserVocabularyEntity u : uvc) {
            responses.add(vocabularyMapper.convertUserVocabularyToResponse(u));
        }
        return responses;
    }

    // update a vocabulary in lesson, system vocab can not be edited
    @Override
    public UserVocabularyResponse updateVocabInLesson(Long lessonId, Long vocabId, VocabularyAddRequest request, Long userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        if (lesson.getUser() == null || !lesson.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, LESSON_NOT_OWNED);
        }
        UserVocabularyEntity uvc = userVocabularyRepository.findByIdAndLesson_Id(vocabId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_VOCAB_NOT_FOUND));
        uvc.setWord(request.getWord());
        uvc.setPos(request.getPos());
        uvc.setAudioPath(request.getAudio_path());
        uvc.setExample(request.getExample());
        uvc.setMeaning(request.getMeaning());
        uvc.setImagePath(request.getImage_path());
        uvc.setPronunciation(request.getPronunciation());
        userVocabularyRepository.save(uvc);
        return vocabularyMapper.convertUserVocabularyToResponse(uvc);
    }

    @Override
    @Transactional
    public void deleteVocabInLesson(Long lessonId, Long vocabId, Long userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        if (lesson.getUser() == null || !lesson.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, LESSON_NOT_OWNED);
        }
        UserVocabularyEntity uvc = userVocabularyRepository.findByIdAndLesson_Id(vocabId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_VOCAB_NOT_FOUND));

        // Delete child records first to avoid foreign key constraint violations
        reviewHistoryRepository.deleteByReviewSchedule_UserVocabulary_Id(uvc.getId());
        reviewScheduleRepository.deleteByUserVocabulary_Id(uvc.getId());

        userVocabularyRepository.delete(uvc);
    }
}
