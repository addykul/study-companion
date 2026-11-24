package dev.studycompanion.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, UUID courseId, String fileName, String contentType,
                               String status, String errorMessage, Instant createdAt) {}
