package com.example.learningVocabularyPlatform.dto.response;

import com.example.learningVocabularyPlatform.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizHistoryResponse {
	private Long quizId;
	private String lessonName;
	private QuizType mode;
	private Integer score;
	private LocalDateTime createdAt;
	private String duration;// Thời gian làm bài

}
