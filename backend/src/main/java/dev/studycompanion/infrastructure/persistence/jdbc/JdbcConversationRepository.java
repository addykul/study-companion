package dev.studycompanion.infrastructure.persistence.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.studycompanion.domain.model.ConversationHistory;
import dev.studycompanion.domain.model.ConversationMessage;
import dev.studycompanion.domain.model.ConversationSource;
import dev.studycompanion.domain.model.ConversationTurn;
import dev.studycompanion.domain.repository.ConversationRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcConversationRepository implements ConversationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcConversationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
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
    public ConversationHistory findLatestForCourse(UUID courseId) {
        List<UUID> conversationIds = jdbcTemplate.query(
                "SELECT id FROM conversations WHERE course_id = ? ORDER BY created_at DESC, id DESC LIMIT 1",
                (rs, rowNum) -> rs.getObject("id", UUID.class), courseId);
        if (conversationIds.isEmpty()) return new ConversationHistory(null, List.of());

        UUID conversationId = conversationIds.getFirst();
        List<ConversationMessage> messages = jdbcTemplate.query("""
                SELECT message.id, message.role, message.content, message.created_at, message.sources_json
                FROM messages message
                JOIN conversations conversation ON conversation.id = message.conversation_id
                WHERE conversation.course_id = ?
                ORDER BY conversation.created_at, message.created_at, message.id
                """, this::mapMessage, courseId);
        return new ConversationHistory(conversationId, messages);
    }

    @Override
    public List<ConversationTurn> findRecentMessages(UUID conversationId, int limit) {
        return jdbcTemplate.query("""
                SELECT role, content FROM (
                    SELECT role, content, created_at, id FROM messages WHERE conversation_id = ?
                    ORDER BY created_at DESC, id DESC LIMIT ?
                ) recent_messages
                ORDER BY created_at ASC, id ASC
                """, (rs, rowNum) -> new ConversationTurn(rs.getString("role"), rs.getString("content")),
                conversationId, limit);
    }

    @Override
    @Transactional
    public void addExchange(UUID conversationId, String question, String answer, List<ConversationSource> sources) {
        insertMessage(conversationId, "USER", question, List.of());
        insertMessage(conversationId, "ASSISTANT", answer, sources);
    }

    private void insertMessage(UUID conversationId, String role, String content, List<ConversationSource> sources) {
        jdbcTemplate.update("""
                INSERT INTO messages(id, conversation_id, role, content, sources_json)
                VALUES (?, ?, ?, ?, ?::jsonb)
                """, UUID.randomUUID(), conversationId, role, content, writeSources(sources));
    }

    private ConversationMessage mapMessage(ResultSet resultSet, int rowNumber) throws SQLException {
        return new ConversationMessage(resultSet.getObject("id", UUID.class), resultSet.getString("role"),
                resultSet.getString("content"), resultSet.getTimestamp("created_at").toInstant(),
                readSources(resultSet.getString("sources_json")));
    }

    private String writeSources(List<ConversationSource> sources) {
        try {
            return objectMapper.writeValueAsString(sources);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize chat source references.", exception);
        }
    }

    private List<ConversationSource> readSources(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize chat source references.", exception);
        }
    }
}
