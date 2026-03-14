package com.example.learningVocabularyPlatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "lesson")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LessonEntity extends BaseEntity {

    @Column(name = "title", length = 200)
    private String title; // Tiêu đề bài học (cho personal lesson)

    @Column(name = "description", length = 500)
    private String description; // Mô tả bài học (cho personal lesson)

    // User 1 - N Lesson
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Lesson 1 - N user_vocabulary
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserVocabularyEntity> userVocabularies;

    // Lesson 1- N Quiz
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    private List<QuizEntity> quizzes;
}
