package dev.studycompanion.domain.model;

import java.time.Instant;
import java.util.UUID;

public record StudyDocument(UUID id, UUID courseId, String fileName, String contentType,
                            String storagePath, DocumentStatus status, String errorMessage,
                            Instant createdAt) {}
