package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
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

    // update a vocabulary in lesson, system vocab can not be edited
    @Override
    public UserVocabularyResponse updateVocabInLesson(Long vocabId, VocabularyAddRequest request) {
        UserVocabularyEntity uvc = userVocabularyRepository.findById(vocabId).orElseThrow(() -> new RuntimeException("User Vocabulary not found"));
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
    public void deleteVocabInLesson(Long vocabId) {
        userVocabularyRepository.deleteById(vocabId);
    }
}
