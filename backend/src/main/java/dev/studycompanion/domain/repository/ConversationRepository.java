package dev.studycompanion.domain.repository;

import dev.studycompanion.domain.model.ConversationMessage;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository {
    UUID create(UUID courseId);
    boolean existsInCourse(UUID conversationId, UUID courseId);
    List<ConversationMessage> findRecentMessages(UUID conversationId, int limit);
    void addExchange(UUID conversationId, String question, String answer);
}
