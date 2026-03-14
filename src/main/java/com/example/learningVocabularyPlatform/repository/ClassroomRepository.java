package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ClassroomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<ClassroomEntity, Long> {
    List<ClassroomEntity> findByOwnerId(Long ownerId);

    List<ClassroomEntity> findByNameContaining(String name);
}
