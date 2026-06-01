package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ClassBoardCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassBoardCommentRepository extends JpaRepository<ClassBoardCommentEntity, Long> {
    List<ClassBoardCommentEntity> findByPost_IdOrderByCreatedAtAsc(Long postId);
}