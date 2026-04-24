package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.SubmissionAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionAttachmentRepository extends JpaRepository<SubmissionAttachmentEntity, Long> {

    List<SubmissionAttachmentEntity> findBySubmission_Id(Long submissionId);
}
