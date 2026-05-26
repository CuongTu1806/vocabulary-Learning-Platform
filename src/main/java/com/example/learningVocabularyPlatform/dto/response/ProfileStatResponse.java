package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProfileStatResponse {
    // Personal info
    private String username;
    private String email;
    private String createdAt;
    
    // Ranking info
    private String currentRank;
    private Integer currentRankPoints;
    private Integer maxRankPoints;
    private String maxRank;
    private Integer contestsParticipated;
    
    // Activity info
    private Integer daysOnlineThisMonth;
    private Integer currentStreak;
    
    // Chart stats
    private ReviewsStatResponse reviews;
    private TimeStatResponse time;
    private CardCountStatResponse cardCount;
    private ReviewIntervalStatResponse reviewInterval;
    private CardEaseStatResponse cardEase;
    private AddStatResponse add;
}
