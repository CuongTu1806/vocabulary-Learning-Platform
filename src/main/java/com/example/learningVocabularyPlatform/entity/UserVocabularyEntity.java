package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "user_vocabulary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserVocabularyEntity extends BaseEntity {

    @Column(name = "word", nullable = false, length = 100)
    private String word;

    @Column(name = "pronunciation", length = 200)
    private String pronunciation;

    @Column(name = "pos", length = 50)
    private String pos;

    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    private String meaning; // Nghĩa tiếng Việt

    @Column(name = "example")
    private String example;

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "image_path")
    private String imagePath;

    // User 1 - N user_vocabulary
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Lesson 1 - N user_vocabulary
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    // Vocabulary 1 - N user_vocabulary
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private VocabularyEntity vocabulary;

    // User_vocabulary 1 - N quiz_result
    @OneToMany(mappedBy = "userVocabulary", fetch = FetchType.LAZY)
    private List<QuizResultEntity> quizResults;

    // User_vocabulary 1 - N Review_schedule
    @OneToMany(mappedBy = "userVocabulary", fetch = FetchType.LAZY)
    private List<ReviewScheduleEntity> reviewSchedules;

    // user_vocabulary 1 - N Vocabulary_stat
    @OneToMany(mappedBy = "userVocabulary", fetch = FetchType.LAZY)
    private List<VocabularyStatEntity> vocabularyStats;
}
