package dev.studycompanion.domain.model;

import java.util.UUID;

public record DocumentChunk(UUID id, UUID courseId, UUID documentId, Integer pageNumber,
                            int chunkIndex, String content, float[] embedding) {}
