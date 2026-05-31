package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ClassBoardPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassBoardPostRepository extends JpaRepository<ClassBoardPostEntity, Long> {
    List<ClassBoardPostEntity> findByClassroom_IdOrderByCreatedAtDesc(Long classroomId);
}