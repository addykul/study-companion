package dev.studycompanion.infrastructure.persistence.jdbc;

import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.domain.model.StudyDocument;
import dev.studycompanion.domain.repository.DocumentRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDocumentRepository implements DocumentRepository {
    private static final RowMapper<StudyDocument> DOCUMENT_ROW_MAPPER = (rs, rowNum) -> new StudyDocument(
            rs.getObject("id", UUID.class), rs.getObject("course_id", UUID.class),
            rs.getString("file_name"), rs.getString("content_type"), rs.getString("storage_path"),
            DocumentStatus.valueOf(rs.getString("status")), rs.getString("error_message"),
            rs.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public StudyDocument saveProcessing(StudyDocument document) {
        Timestamp createdAt = jdbcTemplate.queryForObject("""
                INSERT INTO documents(id, course_id, file_name, content_type, storage_path, status)
                VALUES (?, ?, ?, ?, ?, 'PROCESSING') RETURNING created_at
                """, Timestamp.class, document.id(), document.courseId(), document.fileName(),
                document.contentType(), document.storagePath());
        return new StudyDocument(document.id(), document.courseId(), document.fileName(),
                document.contentType(), document.storagePath(), DocumentStatus.PROCESSING, null,
                createdAt.toInstant());
    }

    @Override
    public List<StudyDocument> findByCourseId(UUID courseId) {
        return jdbcTemplate.query(documentSelect() + " WHERE course_id = ? ORDER BY created_at DESC",
                DOCUMENT_ROW_MAPPER, courseId);
    }

    @Override
    public Optional<StudyDocument> findById(UUID id) {
        return jdbcTemplate.query(documentSelect() + " WHERE id = ?", DOCUMENT_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public void updateStatus(UUID id, DocumentStatus status, String errorMessage) {
        jdbcTemplate.update("UPDATE documents SET status = ?, error_message = ? WHERE id = ?",
                status.name(), errorMessage, id);
    }

    private String documentSelect() {
        return """
                SELECT id, course_id, file_name, content_type, storage_path, status, error_message, created_at
                FROM documents
                """;
    }
}
