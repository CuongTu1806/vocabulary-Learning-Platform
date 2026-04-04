package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.AssignmentEntity;
import com.example.learningVocabularyPlatform.entity.AssignmentSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmissionEntity, Long> {
    List<AssignmentSubmissionEntity> findByAssignment_Id(Long assignmentId);

    Optional<AssignmentSubmissionEntity> findByAssignment_IdAndUser_Id(Long assignmentId, Long userId);
}
