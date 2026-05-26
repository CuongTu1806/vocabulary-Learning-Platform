package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.config.CurrentUserResolver;
import com.example.learningVocabularyPlatform.dto.response.ProfileStatResponse;
import com.example.learningVocabularyPlatform.service.ProfileStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final ProfileStatService profileStatService;
    private final CurrentUserResolver currentUserResolver;
    
    @GetMapping("/stats")
    public ResponseEntity<ProfileStatResponse> getProfileStats(
            Authentication authentication,
            @RequestParam(defaultValue = "month") String period) {
        Long userId = currentUserResolver.requireUserId(authentication);
        ProfileStatResponse stats = profileStatService.getProfileStats(userId, period);
        return ResponseEntity.ok(stats);
    }
}
