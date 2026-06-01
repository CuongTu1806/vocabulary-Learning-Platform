package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.AssignmentSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmissionEntity, Long> {
    List<AssignmentSubmissionEntity> findByAssignment_Id(Long assignmentId);

    @Query(
            "SELECT DISTINCT s FROM AssignmentSubmissionEntity s "
                    + "LEFT JOIN FETCH s.attachments "
                    + "WHERE s.assignment.id = :assignmentId")
    List<AssignmentSubmissionEntity> findByAssignmentIdWithAttachments(@Param("assignmentId") Long assignmentId);

    Optional<AssignmentSubmissionEntity> findByAssignment_IdAndUser_Id(Long assignmentId, Long userId);

    @Query(
            "SELECT s FROM AssignmentSubmissionEntity s LEFT JOIN FETCH s.attachments WHERE s.id = :id")
    Optional<AssignmentSubmissionEntity> findByIdWithAttachments(@Param("id") Long id);

    @Query(
            "SELECT s FROM AssignmentSubmissionEntity s "
                    + "JOIN FETCH s.assignment a "
                    + "LEFT JOIN FETCH a.classroom c "
                    + "LEFT JOIN FETCH c.owner "
                    + "LEFT JOIN FETCH a.userCreated "
                    + "JOIN FETCH s.user "
                    + "WHERE s.id = :id")
    Optional<AssignmentSubmissionEntity> findByIdWithAssignmentDetails(@Param("id") Long id);
}
