package dev.studycompanion.infrastructure.persistence.jpa.entity;

import dev.studycompanion.domain.model.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class CourseJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CourseJpaEntity() {}

    public CourseJpaEntity(UUID id, String name, String description, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public static CourseJpaEntity fromDomain(Course course) {
        return new CourseJpaEntity(course.id(), course.name(), course.description(), course.createdAt());
    }

    public Course toDomain(int documentCount) {
        return new Course(id, name, description, createdAt, documentCount);
    }
}
