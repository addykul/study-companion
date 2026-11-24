package dev.studycompanion.application.service;

import dev.studycompanion.application.exception.AppException;
import dev.studycompanion.application.model.DocumentUploadCommand;
import dev.studycompanion.application.model.StoredDocumentFile;
import dev.studycompanion.application.port.DocumentStorage;
import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.domain.model.StudyDocument;
import dev.studycompanion.domain.repository.DocumentRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    private static final int MAX_UPLOAD_BYTES = 10 * 1024 * 1024;

    private final CourseService courseService;
    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final DocumentIngestionService ingestionService;

    public DocumentService(CourseService courseService, DocumentRepository documentRepository,
                           DocumentStorage documentStorage, DocumentIngestionService ingestionService) {
        this.courseService = courseService;
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
        this.ingestionService = ingestionService;
    }

    public StudyDocument upload(UUID courseId, DocumentUploadCommand command) {
        courseService.get(courseId);
        validateUpload(command);
        String fileName = sanitizeFileName(command.originalFileName());
        boolean isPdf = fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
        UUID documentId = UUID.randomUUID();
        StoredDocumentFile storedFile = documentStorage.store(courseId, documentId, fileName, command.content());

        StudyDocument processingDocument = new StudyDocument(documentId, courseId, fileName,
                storedFile.contentType(), storedFile.path().toString(), DocumentStatus.PROCESSING, null, null);
        StudyDocument savedDocument;
        try {
            savedDocument = documentRepository.saveProcessing(processingDocument);
        } catch (RuntimeException exception) {
            documentStorage.delete(storedFile);
            throw exception;
        }
        try {
            ingestionService.processAsync(documentId, courseId, storedFile.path(), isPdf);
            return savedDocument;
        } catch (RuntimeException exception) {
            documentRepository.updateStatus(documentId, DocumentStatus.FAILED,
                    "Document processing could not be scheduled. Please try uploading again.");
            documentStorage.delete(storedFile);
            throw AppException.serviceUnavailable("Document processing is busy. Please try again shortly.");
        }
    }

    public List<StudyDocument> list(UUID courseId) {
        courseService.get(courseId);
        return documentRepository.findByCourseId(courseId);
    }

    public StudyDocument get(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> AppException.notFound("Document not found."));
    }

    private void validateUpload(DocumentUploadCommand command) {
        if (command == null || command.contentSize() == 0) {
            throw AppException.badRequest("Choose a non-empty PDF or text file.");
        }
        if (command.contentSize() > MAX_UPLOAD_BYTES) {
            throw AppException.badRequest("Files must be 10 MB or smaller.");
        }
        String fileName = sanitizeFileName(command.originalFileName()).toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".txt")) {
            throw AppException.badRequest("Only PDF and .txt files are supported.");
        }
    }

    private String sanitizeFileName(String originalFileName) {
        String normalizedName = originalFileName == null ? "upload" : originalFileName.replace('\\', '/');
        String baseName = Path.of(normalizedName).getFileName().toString();
        return baseName.replaceAll("[^A-Za-z0-9._ -]", "_");
    }
}
