package dev.studycompanion.application.model;

import java.nio.file.Path;

public record StoredDocumentFile(Path path, String contentType) {}
