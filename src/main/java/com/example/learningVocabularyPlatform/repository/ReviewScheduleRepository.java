package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ReviewScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewScheduleRepository extends JpaRepository<ReviewScheduleEntity, Long> {
    Optional<ReviewScheduleEntity> findTopByUserVocabulary_IdOrderByUpdatedAtDesc(Long userVocabularyId);

    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(
	    Long userId,
	    LocalDateTime now
    );

    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndNextReviewDateBetweenOrderByNextReviewDateAsc(
	    Long userId,
	    LocalDateTime start,
	    LocalDateTime end
    );

    long countByUserVocabulary_User_IdAndNextReviewDateLessThanEqualAndStateIn(
	    Long userId,
	    LocalDateTime now,
	    List<String> states
    );
}
