package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.service.UserVocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vocabulary")
public class UserVocabularyController {

    private final UserVocabularyService userVocabularyService;

    @GetMapping("/lesson/{lesson_id}")
    public List<UserVocabularyResponse> getVocabInLesson(@PathVariable Long lesson_id){
        return userVocabularyService.getVocabInLesson(lesson_id);
    }

    @GetMapping("/search")
    public List<UserVocabularyResponse> searchVocabulary(@RequestParam String query){
        return userVocabularyService.searchVocabulary(query);
    }
}
