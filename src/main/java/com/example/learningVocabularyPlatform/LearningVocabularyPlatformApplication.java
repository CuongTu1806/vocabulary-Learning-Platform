package com.example.learningVocabularyPlatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LearningVocabularyPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningVocabularyPlatformApplication.class, args);
		System.out.println("Welcome to Vocabulary Learning Platform Application");
	}
}
