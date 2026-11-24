package dev.studycompanion.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatMessageResponse(UUID id, String role, String content, Instant createdAt,
                                 List<SourceReference> sources) {
    public ChatMessageResponse {
        sources = List.copyOf(sources);
    }
}
