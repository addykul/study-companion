package dev.studycompanion.domain.model;

import java.util.List;
import java.util.UUID;

public record ConversationHistory(UUID conversationId, List<ConversationMessage> messages) {
    public ConversationHistory {
        messages = List.copyOf(messages);
    }
}
