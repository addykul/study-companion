package dev.studycompanion.application.port;

import dev.studycompanion.application.model.StoredDocumentFile;
import java.util.UUID;

public interface DocumentStorage {
    StoredDocumentFile store(UUID courseId, UUID documentId, String fileName, byte[] content);
    void delete(StoredDocumentFile file);
}
