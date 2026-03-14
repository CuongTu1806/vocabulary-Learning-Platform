package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import com.example.learningVocabularyPlatform.mapper.VocabularyMapper;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import com.example.learningVocabularyPlatform.repository.VocabularyRepository;
import com.example.learningVocabularyPlatform.service.UserVocabularyService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserVocabularyServiceImpl implements UserVocabularyService {
    private final UserVocabularyRepository userVocabularyRepository;
    private final VocabularyMapper vocabularyMapper;
    private final VocabularyRepository vocabularyRepository;

    @Override
    public List<UserVocabularyResponse> getVocabInLesson(Long lessonId) {
        List<UserVocabularyEntity> list = userVocabularyRepository.findByLesson_Id(lessonId);
        List<UserVocabularyResponse> responses = new ArrayList<>();

        for(UserVocabularyEntity uvc : list){
            if(uvc.getVocabulary() != null){
                responses.add(vocabularyMapper.convertVocabularyToResponse(uvc.getVocabulary()));
            }
            else responses.add(vocabularyMapper.convertUserVocabularyToResponse(uvc));
            responses.getLast().setLessonId(lessonId);
        }
        return responses;
    }

    @Override
    public List<UserVocabularyResponse> searchVocabulary(String keyword) {
        List<VocabularyEntity> vc = vocabularyRepository.findByWord(keyword);
        List<UserVocabularyEntity> uvc = userVocabularyRepository.findByWord(keyword);
        List<UserVocabularyResponse> responses = new ArrayList<>();
        for(VocabularyEntity v : vc){
            responses.add(vocabularyMapper.convertVocabularyToResponse(v));
        }
        for(UserVocabularyEntity u : uvc){
            responses.add(vocabularyMapper.convertUserVocabularyToResponse(u));
        }
        return responses;
    }
}
