package dev.studycompanion.api.dto.response;

import java.util.List;
import java.util.UUID;

public record ChatResponse(UUID conversationId, String answer, List<SourceReference> sources) {}
