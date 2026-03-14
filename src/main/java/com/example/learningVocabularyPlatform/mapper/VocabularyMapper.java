package com.example.learningVocabularyPlatform.mapper;

import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.request.VocabularyRequest;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import org.antlr.v4.runtime.Vocabulary;
import org.springframework.stereotype.Component;

@Component
public class VocabularyMapper {

    // map vocabulary -> res
    public UserVocabularyResponse convertVocabularyToResponse(VocabularyEntity vc) {
        return UserVocabularyResponse.builder()
                .id(vc.getId())
                .word(vc.getWord())
                .audio_path(vc.getAudioPath())
                .image_path(vc.getImagePath())
                .pos(vc.getPos())
                .meaning(vc.getMeaning())
                .example(vc.getExample())
                .pronunciation(vc.getPronunciation())
                .type("system")
                .build();
    }

    public UserVocabularyResponse convertUserVocabularyToResponse(UserVocabularyEntity uvc) {
        return UserVocabularyResponse.builder()
                .id(uvc.getId())
                .word(uvc.getWord())
                .audio_path(uvc.getAudioPath())
                .image_path(uvc.getImagePath())
                .pos(uvc.getPos())
                .meaning(uvc.getMeaning())
                .example(uvc.getExample())
                .pronunciation(uvc.getPronunciation())
                .type("user")
                .status(uvc.getStatus())
                .build();
    }

    public UserVocabularyEntity convertRequestToUserVocab(VocabularyAddRequest request) {
        return UserVocabularyEntity.builder()
                .pos(request.getPos())
                .example(request.getExample())
                .word(request.getWord())
                .audioPath(request.getAudio_path())
                .imagePath(request.getImage_path())
                .meaning(request.getMeaning())
                .pronunciation(request.getPronunciation())
                .build();
    }
}
