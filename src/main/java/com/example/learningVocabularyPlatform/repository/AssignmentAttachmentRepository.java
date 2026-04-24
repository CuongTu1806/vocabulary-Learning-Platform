package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.AssignmentAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentAttachmentRepository extends JpaRepository<AssignmentAttachmentEntity, Long> {

    List<AssignmentAttachmentEntity> findByAssignment_Id(Long assignmentId);
}
