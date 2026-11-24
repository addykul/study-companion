package dev.studycompanion.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationMessage(UUID id, String role, String content, Instant createdAt,
                                  List<ConversationSource> sources) {
    public ConversationMessage {
        sources = List.copyOf(sources);
    }
}
