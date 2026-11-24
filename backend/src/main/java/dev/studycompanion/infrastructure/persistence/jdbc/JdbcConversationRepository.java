package dev.studycompanion.infrastructure.persistence.jdbc;

import dev.studycompanion.domain.model.ConversationMessage;
import dev.studycompanion.domain.repository.ConversationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcConversationRepository implements ConversationRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcConversationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UUID create(UUID courseId) {
        UUID conversationId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO conversations(id, course_id) VALUES (?, ?)", conversationId, courseId);
        return conversationId;
    }

    @Override
    public boolean existsInCourse(UUID conversationId, UUID courseId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM conversations WHERE id = ? AND course_id = ?",
                Integer.class, conversationId, courseId);
        return count != null && count > 0;
    }

    @Override
    public List<ConversationMessage> findRecentMessages(UUID conversationId, int limit) {
        return jdbcTemplate.query("""
                SELECT role, content FROM (
                    SELECT role, content, created_at FROM messages WHERE conversation_id = ?
                    ORDER BY created_at DESC LIMIT ?
                ) recent_messages
                ORDER BY created_at ASC
                """, (rs, rowNum) -> new ConversationMessage(rs.getString("role"), rs.getString("content")),
                conversationId, limit);
    }

    @Override
    @Transactional
    public void addExchange(UUID conversationId, String question, String answer) {
        insertMessage(conversationId, "USER", question);
        insertMessage(conversationId, "ASSISTANT", answer);
    }

    private void insertMessage(UUID conversationId, String role, String content) {
        jdbcTemplate.update("INSERT INTO messages(id, conversation_id, role, content) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), conversationId, role, content);
    }
}
