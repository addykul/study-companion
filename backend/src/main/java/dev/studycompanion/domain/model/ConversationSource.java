package dev.studycompanion.domain.model;

import java.util.UUID;

public record ConversationSource(UUID documentId, String documentName, Integer page, String excerpt) {}
