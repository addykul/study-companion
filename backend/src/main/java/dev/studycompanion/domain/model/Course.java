package dev.studycompanion.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Course(UUID id, String name, String description, Instant createdAt, int documentCount) {}
