package dev.studycompanion.domain.repository;

import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.domain.model.StudyDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    StudyDocument saveProcessing(StudyDocument document);
    List<StudyDocument> findByCourseId(UUID courseId);
    Optional<StudyDocument> findById(UUID id);
    void updateStatus(UUID id, DocumentStatus status, String errorMessage);
}
