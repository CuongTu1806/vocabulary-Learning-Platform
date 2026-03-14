package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.mapper.LessonMapper;
import com.example.learningVocabularyPlatform.mapper.VocabularyMapper;
import com.example.learningVocabularyPlatform.repository.LessonRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import com.example.learningVocabularyPlatform.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;
    private final UserVocabularyRepository userVocabularyRepository;

    //Get all lesson belong to user
    @Override
    public List<LessonResponse> getAll(Long userId) {
        List<LessonEntity> lessons = lessonRepository.findByUser_Id(userId);
        List<LessonResponse> res = new ArrayList<>();
        for(LessonEntity lesson : lessons) {
            res.add(lessonMapper.convertLessonToResponse(lesson));
        }
        return res;
    }

    // create new lesson
    @Override
    public LessonResponse createLesson(Long userId, LessonRequest lessonRequest) {
        LessonEntity ls = lessonMapper.convertRequestToLessonEntity(lessonRequest);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ls.setUser(user);
        ls = lessonRepository.save(ls);
        return lessonMapper.convertLessonToResponse(ls);
    }

    // add a vocabulary into lesson
    @Override
    public UserVocabularyResponse addVocab(Long lessonId, VocabularyAddRequest request, Long userId) {
        UserVocabularyEntity uvc = vocabularyMapper.convertRequestToUserVocab(request);
        LessonEntity lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Lesson not found"));
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        uvc.setUser(user);
        uvc.setLesson(lesson);
        userVocabularyRepository.save(uvc);
        return vocabularyMapper.convertUserVocabularyToResponse(uvc);
    }

    // update title and description of lesson
    @Override
    public LessonResponse updateLesson(Long userId, Long lessonId, LessonRequest request) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Lesson not found"));
        if(request.getTitle() != null) lesson.setTitle(request.getTitle());
        if(request.getDescription() != null) lesson.setDescription(request.getDescription());
        lessonRepository.save(lesson);
        return lessonMapper.convertLessonToResponse(lesson);
    }

    // delete a lesson, it will delete all vocabulary of this lesson
    @Override
    public void deleteLesson(Long userId, Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }
}
