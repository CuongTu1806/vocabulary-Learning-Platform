package com.example.learningVocabularyPlatform.dto.response;

public class ProfileResponse {
    private String name;
    private String email;
    private String phoneNumber;
    private String dateStarted;
    private String avatarUrl;

    // Thong ke ranking
    private int ranking;
    private int scoreContest;
    private int maxRanking;
    private int maxScore;
    private int numberOfContests;

    // thong ke qua trinh hoc
    private int numberOfWords; // số lượng từ đã học
    private int numberOfLesson;
    private int maxStreak;
    private int streak;
    private double accuracy; // s lân ôn tập mà khogno bị qun;

}
