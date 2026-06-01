package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ContestProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestProblemRepository extends JpaRepository<ContestProblemEntity, Long> {

    /** Thứ tự hiển thị câu hỏi theo orderIndex */
    List<ContestProblemEntity> findByContest_IdOrderByOrderIndexAsc(Long contestId);
}
