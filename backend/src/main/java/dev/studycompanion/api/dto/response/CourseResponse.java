package dev.studycompanion.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CourseResponse(UUID id, String name, String description, Instant createdAt, int documentCount) {}
