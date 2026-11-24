package dev.studycompanion.infrastructure.persistence.jdbc;

import dev.studycompanion.domain.model.Course;
import dev.studycompanion.domain.repository.CourseRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCourseRepository implements CourseRepository {
    private static final RowMapper<Course> COURSE_ROW_MAPPER = (rs, rowNum) -> new Course(
            rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("description"),
            rs.getTimestamp("created_at").toInstant(), rs.getInt("document_count"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcCourseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Course save(Course course) {
        Timestamp createdAt = jdbcTemplate.queryForObject(
                "INSERT INTO courses(id, name, description) VALUES (?, ?, ?) RETURNING created_at",
                Timestamp.class, course.id(), course.name(), course.description());
        return new Course(course.id(), course.name(), course.description(), createdAt.toInstant(), 0);
    }

    @Override
    public List<Course> findAll() {
        return jdbcTemplate.query(courseSelect() + " GROUP BY c.id ORDER BY c.created_at DESC", COURSE_ROW_MAPPER);
    }

    @Override
    public Optional<Course> findById(UUID id) {
        return jdbcTemplate.query(courseSelect() + " WHERE c.id = ? GROUP BY c.id", COURSE_ROW_MAPPER, id)
                .stream().findFirst();
    }

    private String courseSelect() {
        return """
                SELECT c.id, c.name, c.description, c.created_at, count(d.id) AS document_count
                FROM courses c LEFT JOIN documents d ON d.course_id = c.id
                """;
    }
}
