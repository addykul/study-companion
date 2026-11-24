package dev.studycompanion.application.service;

import dev.studycompanion.domain.model.Course;
import dev.studycompanion.domain.repository.CourseRepository;
import dev.studycompanion.application.exception.AppException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course create(String requestedName, String requestedDescription) {
        String name = requestedName == null ? "" : requestedName.trim();
        String description = requestedDescription == null ? "" : requestedDescription.trim();
        if (!StringUtils.hasText(name)) throw AppException.badRequest("Course name is required.");
        if (name.length() > 120) throw AppException.badRequest("Course name must be 120 characters or fewer.");
        if (description.length() > 500) throw AppException.badRequest("Description must be 500 characters or fewer.");

        Course newCourse = new Course(UUID.randomUUID(), name, description, null, 0);
        return courseRepository.save(newCourse);
    }

    public List<Course> list() {
        return courseRepository.findAll();
    }

    public Course get(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> AppException.notFound("Course not found."));
    }
}
