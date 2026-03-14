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

    @GetMapping("/search")
    public List<UserVocabularyResponse> searchVocabulary(@RequestParam String query){
        return userVocabularyService.searchVocabulary(query);
    }
}
