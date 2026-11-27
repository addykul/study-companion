package dev.studycompanion.infrastructure.persistence.jpa.entity;

import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.domain.model.StudyDocument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "storage_path", nullable = false, columnDefinition = "text")
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DocumentJpaEntity() {}

    public DocumentJpaEntity(UUID id, UUID courseId, String fileName, String contentType,
                             String storagePath, DocumentStatus status, String errorMessage,
                             Instant createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.storagePath = storagePath;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public static DocumentJpaEntity fromDomain(StudyDocument document) {
        return new DocumentJpaEntity(document.id(), document.courseId(), document.fileName(),
                document.contentType(), document.storagePath(), document.status(),
                document.errorMessage(), document.createdAt());
    }

    public StudyDocument toDomain() {
        return new StudyDocument(id, courseId, fileName, contentType, storagePath,
                status, errorMessage, createdAt);
    }
}
