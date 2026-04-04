package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ClassMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMemberEntity, Long> {
    List<ClassMemberEntity> findByClassroomId(Long classroomId);

    @Transactional
    void deleteByClassroomIdAndUserId(Long classroomId, Long userId);

    @Transactional
    void deleteByClassroomId(Long classroomId);

    boolean existsByClassroomIdAndUserId(Long classroomId, Long userId);
}
