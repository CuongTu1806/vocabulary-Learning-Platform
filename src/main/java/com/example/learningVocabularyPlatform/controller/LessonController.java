package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.config.CurrentUserResolver;
import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.service.LessonService;
import com.example.learningVocabularyPlatform.service.UserVocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final UserVocabularyService userVocabularyService;
    private final CurrentUserResolver currentUserResolver;

    // Get all lessons belong to user
    @GetMapping("")
    public List<LessonResponse> getAllLessons(Authentication authentication) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.getAll(userId);
    }

    @GetMapping("/public/search")
    public List<LessonResponse> searchPublicLessons(@RequestParam(required = false) String query) {
        return lessonService.searchPublicLessons(query);
    }

    @GetMapping("/{lessonId}")
    public LessonResponse getLesson(Authentication authentication, @PathVariable Long lessonId) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.getLesson(lessonId, userId);
    }

    @PostMapping("/{lessonId}/download")
    public LessonResponse importLesson(Authentication authentication, @PathVariable Long lessonId) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.importLesson(lessonId, userId);
    }

    // create lesson
    @PostMapping("")
    public LessonResponse createLesson(Authentication authentication, @RequestBody LessonRequest lessonRequest) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.createLesson(userId, lessonRequest);
    }

    // update lesson
    @PutMapping("/{lessonId}")
    public LessonResponse updateLesson(Authentication authentication,
                                       @PathVariable Long lessonId,
                                       @RequestBody LessonRequest lessonRequest) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.updateLesson(userId, lessonId, lessonRequest);
    }

    // delete lesson
    @DeleteMapping("/{lessonId}")
    public void deleteLesson(Authentication authentication, @PathVariable Long lessonId) {
        Long userId = currentUserResolver.requireUserId(authentication);
        lessonService.deleteLesson(userId, lessonId);
    }

    // add vocab, only vocab belong to user
    @PostMapping("{lessonId}/vocabularies")
    public UserVocabularyResponse addVocab(Authentication authentication,
                                           @PathVariable Long lessonId,
                                           @RequestBody VocabularyAddRequest request) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.addVocab(lessonId, request, userId);
    }

    // hiển thị toàn bộ vocab trong lesson
    @GetMapping("/{lessonId}/vocabularies")
    public List<UserVocabularyResponse> getVocabInLesson(Authentication authentication, @PathVariable Long lessonId){
        Long userId = currentUserResolver.requireUserId(authentication);
        return lessonService.getVocabInLesson(lessonId, userId);
    }

    // update a vocabulary in lesson, system vocab can not be edited
    @PutMapping("/{lessonId}/vocabularies/{vocabId}")
    public UserVocabularyResponse updateVocabInLesson(Authentication authentication,
                                              @PathVariable Long lessonId,
                                              @PathVariable Long vocabId,
                                              @RequestBody VocabularyAddRequest request) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return userVocabularyService.updateVocabInLesson(lessonId, vocabId, request, userId);
    }

    // delete a vocab in lesson
    @DeleteMapping("/{lessonId}/vocabularies/{vocabId}")
    public void deleteVocabInLesson(Authentication authentication, @PathVariable Long lessonId, @PathVariable Long vocabId) {
        Long userId = currentUserResolver.requireUserId(authentication);
        userVocabularyService.deleteVocabInLesson(lessonId, vocabId, userId);
    }
}
