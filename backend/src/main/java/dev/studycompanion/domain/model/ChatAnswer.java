package dev.studycompanion.domain.model;

import java.util.List;
import java.util.UUID;

public record ChatAnswer(UUID conversationId, String answer, List<RetrievedChunk> sources) {
    public ChatAnswer { sources = List.copyOf(sources); }
}
