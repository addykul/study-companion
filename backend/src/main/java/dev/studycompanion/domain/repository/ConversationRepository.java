package dev.studycompanion.domain.repository;

import dev.studycompanion.domain.model.ConversationHistory;
import dev.studycompanion.domain.model.ConversationMessage;
import dev.studycompanion.domain.model.ConversationSource;
import dev.studycompanion.domain.model.ConversationTurn;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository {
    UUID create(UUID courseId);
    boolean existsInCourse(UUID conversationId, UUID courseId);
    ConversationHistory findLatestForCourse(UUID courseId);
    List<ConversationTurn> findRecentMessages(UUID conversationId, int limit);
    void addExchange(UUID conversationId, String question, String answer, List<ConversationSource> sources);
}
