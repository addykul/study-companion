package dev.studycompanion.infrastructure.persistence.jpa;

import dev.studycompanion.domain.model.Course;
import dev.studycompanion.domain.repository.CourseRepository;
import dev.studycompanion.infrastructure.persistence.jpa.entity.CourseJpaEntity;
import dev.studycompanion.infrastructure.persistence.jpa.projection.CourseSummaryProjection;
import dev.studycompanion.infrastructure.persistence.jpa.repository.CourseJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCourseRepositoryAdapter implements CourseRepository {
    private final CourseJpaRepository courseJpaRepository;

    public JpaCourseRepositoryAdapter(CourseJpaRepository courseJpaRepository) {
        this.courseJpaRepository = courseJpaRepository;
    }

    @Override
    @Transactional
    public Course save(Course course) {
        CourseJpaEntity savedCourse = courseJpaRepository.saveAndFlush(CourseJpaEntity.fromDomain(course));
        return savedCourse.toDomain(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseJpaRepository.findCourseSummaries().stream()
                .map(JpaCourseRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findById(UUID id) {
        return courseJpaRepository.findCourseSummaryById(id).map(JpaCourseRepositoryAdapter::toDomain);
    }

    private static Course toDomain(CourseSummaryProjection projection) {
        long documentCount = projection.getDocumentCount() == null ? 0 : projection.getDocumentCount();
        return new Course(projection.getId(), projection.getName(), projection.getDescription(),
                projection.getCreatedAt(), Math.toIntExact(documentCount));
    }
}
