package dev.studycompanion.infrastructure.persistence.jpa.repository;

import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.infrastructure.persistence.jpa.entity.DocumentJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, UUID> {
    List<DocumentJpaEntity> findAllByCourseIdOrderByCreatedAtDesc(UUID courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DocumentJpaEntity d set d.status = :status, d.errorMessage = :errorMessage where d.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") DocumentStatus status,
                     @Param("errorMessage") String errorMessage);
}
