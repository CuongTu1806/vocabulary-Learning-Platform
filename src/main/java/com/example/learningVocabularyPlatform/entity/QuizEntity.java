package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "quiz")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizEntity extends BaseEntity {

    @Column(name = "type_quiz")
    @Enumerated(EnumType.STRING)
    private String typeQuiz;

    @Column(name = "score")
    private Integer score;

    // User 1 - N Quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Lesson 1 - N Quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private LessonEntity lesson;

    // Quiz 1 - N Quiz_result
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    private List<QuizResultEntity> quizResults;
}
