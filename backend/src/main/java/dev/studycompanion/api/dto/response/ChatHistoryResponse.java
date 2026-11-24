package dev.studycompanion.api.dto.response;

import java.util.List;
import java.util.UUID;

public record ChatHistoryResponse(UUID conversationId, List<ChatMessageResponse> messages) {
    public ChatHistoryResponse {
        messages = List.copyOf(messages);
    }
}
