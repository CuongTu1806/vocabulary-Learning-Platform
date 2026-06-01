package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.AssignmentSubmissionDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentSubmissionDetailRepository extends JpaRepository<AssignmentSubmissionDetailEntity, Long> {
    List<AssignmentSubmissionDetailEntity> findBySubmission_Id(Long submissionId);

    @Query("SELECT d FROM AssignmentSubmissionDetailEntity d JOIN d.submission s WHERE s.assignment.id = :assignmentId")
    List<AssignmentSubmissionDetailEntity> findByAssignmentId(@Param("assignmentId") Long assignmentId);
}
