package dev.studycompanion.infrastructure.persistence.jpa.projection;

import java.time.Instant;
import java.util.UUID;

public interface CourseSummaryProjection {
    UUID getId();
    String getName();
    String getDescription();
    Instant getCreatedAt();
    Long getDocumentCount();
}
