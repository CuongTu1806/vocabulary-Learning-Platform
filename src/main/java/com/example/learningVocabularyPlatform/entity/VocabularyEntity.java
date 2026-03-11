package com.example.learningVocabularyPlatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "vocabulary")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VocabularyEntity extends BaseEntity {

    @Column(name = "word")
    private String word;

    @Column(name = "pronunciation")
    private String pronunciation;

    @Column(name = "pos")
    private String pos;

    @Column(name = "meaning")
    private String meaning;

    @Column(name = "example")
    private String example;

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "image_path")
    private String imagePath;

    // vocabulary 1 - N user_vocabulary
    @OneToMany(mappedBy = "vocabulary", fetch = FetchType.LAZY)
    private List<UserVocabularyEntity> userVocabularies;
}
