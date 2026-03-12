package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.service.UserVocabularyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vocabularies")
public class UserVocabularyController {

    private UserVocabularyService userVocabularyService;

    @GetMapping("lesson/{lessonId}")
    public List<UserVocabularyEntity> getVocabularyByLesson(@PathVariable Long lessonId) {
        return UserVocabularyService.getVocabularyByLesson(lessonId);
    }
}
