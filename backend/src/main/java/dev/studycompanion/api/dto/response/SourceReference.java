package dev.studycompanion.api.dto.response;

import java.util.UUID;

public record SourceReference(UUID documentId, String documentName, Integer page, String excerpt) {}
