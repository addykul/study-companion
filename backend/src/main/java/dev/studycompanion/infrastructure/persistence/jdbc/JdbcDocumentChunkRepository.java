package dev.studycompanion.infrastructure.persistence.jdbc;

import dev.studycompanion.domain.model.DocumentChunk;
import dev.studycompanion.domain.model.RetrievedChunk;
import dev.studycompanion.domain.repository.DocumentChunkRepository;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDocumentChunkRepository implements DocumentChunkRepository {
    private static final int WRITE_BATCH_SIZE = 32;
    private static final RowMapper<RetrievedChunk> RETRIEVED_CHUNK_ROW_MAPPER = (rs, rowNum) ->
            new RetrievedChunk(rs.getObject("id", UUID.class), rs.getObject("document_id", UUID.class),
                    rs.getString("file_name"), (Integer) rs.getObject("page_number"),
                    rs.getString("content"), rs.getDouble("similarity"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<DocumentChunk> chunks) {
        jdbcTemplate.batchUpdate("""
                        INSERT INTO document_chunks(id, course_id, document_id, page_number, chunk_index, content, embedding)
                        VALUES (?, ?, ?, ?, ?, ?, ?::vector)
                        """, chunks, WRITE_BATCH_SIZE, this::bindChunk);
    }

    @Override
    public void deleteByDocumentId(UUID documentId) {
        jdbcTemplate.update("DELETE FROM document_chunks WHERE document_id = ?", documentId);
    }

    @Override
    public List<RetrievedChunk> findSimilarReadyByCourse(UUID courseId, float[] queryEmbedding,
                                                          double similarityThreshold, int limit) {
        String vector = PgVectorFormatter.toLiteral(queryEmbedding);
        return jdbcTemplate.query("""
                SELECT dc.id, dc.document_id, d.file_name, dc.page_number, dc.content,
                       1 - (dc.embedding <=> ?::vector) AS similarity
                FROM document_chunks dc
                JOIN documents d ON d.id = dc.document_id
                WHERE dc.course_id = ? AND d.status = 'READY'
                  AND 1 - (dc.embedding <=> ?::vector) >= ?
                ORDER BY dc.embedding <=> ?::vector
                LIMIT ?
                """, RETRIEVED_CHUNK_ROW_MAPPER,
                vector, courseId, vector, similarityThreshold, vector, limit);
    }

    private void bindChunk(PreparedStatement statement, DocumentChunk chunk) throws SQLException {
        statement.setObject(1, chunk.id());
        statement.setObject(2, chunk.courseId());
        statement.setObject(3, chunk.documentId());
        statement.setObject(4, chunk.pageNumber());
        statement.setInt(5, chunk.chunkIndex());
        statement.setString(6, chunk.content());
        statement.setString(7, PgVectorFormatter.toLiteral(chunk.embedding()));
    }
}
