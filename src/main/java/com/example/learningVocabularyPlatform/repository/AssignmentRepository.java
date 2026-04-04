package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
    List<AssignmentEntity> findByClassroom_Id(Long classId);
}
