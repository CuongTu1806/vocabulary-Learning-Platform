package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
    List<AssignmentEntity> findByClassroom_Id(Long classId);

    List<AssignmentEntity> findByUserCreated_Id(Long userId);

    @Query(
            "SELECT a FROM AssignmentEntity a "
                    + "LEFT JOIN FETCH a.classroom c "
                    + "LEFT JOIN FETCH c.owner "
                    + "LEFT JOIN FETCH a.userCreated "
                    + "WHERE a.id = :id")
    Optional<AssignmentEntity> findByIdWithDetails(@Param("id") Long id);
}
