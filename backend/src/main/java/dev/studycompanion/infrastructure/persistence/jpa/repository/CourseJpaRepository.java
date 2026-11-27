package dev.studycompanion.infrastructure.persistence.jpa.repository;

import dev.studycompanion.infrastructure.persistence.jpa.entity.CourseJpaEntity;
import dev.studycompanion.infrastructure.persistence.jpa.entity.DocumentJpaEntity;
import dev.studycompanion.infrastructure.persistence.jpa.projection.CourseSummaryProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseJpaRepository extends JpaRepository<CourseJpaEntity, UUID> {
    @Query("""
            select c.id as id, c.name as name, c.description as description,
                   c.createdAt as createdAt, count(d.id) as documentCount
            from CourseJpaEntity c
            left join DocumentJpaEntity d on d.courseId = c.id
            group by c.id, c.name, c.description, c.createdAt
            order by c.createdAt desc
            """)
    List<CourseSummaryProjection> findCourseSummaries();

    @Query("""
            select c.id as id, c.name as name, c.description as description,
                   c.createdAt as createdAt, count(d.id) as documentCount
            from CourseJpaEntity c
            left join DocumentJpaEntity d on d.courseId = c.id
            where c.id = :courseId
            group by c.id, c.name, c.description, c.createdAt
            """)
    Optional<CourseSummaryProjection> findCourseSummaryById(@Param("courseId") UUID courseId);
}
