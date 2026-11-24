package dev.studycompanion.domain.repository;

import dev.studycompanion.domain.model.Course;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository {
    Course save(Course course);
    List<Course> findAll();
    Optional<Course> findById(UUID id);
}
