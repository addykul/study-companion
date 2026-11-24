package dev.studycompanion.domain.model;

import java.util.UUID;

public record RetrievedChunk(UUID chunkId, UUID documentId, String documentName,
                             Integer page, String content, double similarity) {}
