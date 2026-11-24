package dev.studycompanion.infrastructure.storage;

import dev.studycompanion.application.exception.AppException;
import dev.studycompanion.application.model.StoredDocumentFile;
import dev.studycompanion.application.port.DocumentStorage;
import dev.studycompanion.config.AppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalDocumentStorage implements DocumentStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDocumentStorage.class);
    private final Path uploadRoot;

    public LocalDocumentStorage(AppProperties appProperties) {
        this.uploadRoot = Path.of(appProperties.uploadDir()).toAbsolutePath().normalize();
    }

    @Override
    public StoredDocumentFile store(UUID courseId, UUID documentId, String fileName, byte[] content) {
        boolean isPdf = fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
        Path courseDirectory = uploadRoot.resolve(courseId.toString()).normalize();
        Path destination = courseDirectory.resolve(documentId + (isPdf ? ".pdf" : ".txt")).normalize();
        if (!destination.startsWith(uploadRoot)) throw AppException.badRequest("Invalid file name.");
        try {
            Files.createDirectories(courseDirectory);
            Files.write(destination, content);
            return new StoredDocumentFile(destination, isPdf ? "application/pdf" : "text/plain");
        } catch (IOException exception) {
            LOGGER.error("Could not store uploaded document for course {}", courseId, exception);
            throw AppException.internalServerError("Could not store the uploaded file.");
        }
    }

    @Override
    public void delete(StoredDocumentFile file) {
        try {
            Files.deleteIfExists(file.path());
        } catch (IOException exception) {
            LOGGER.warn("Could not clean up uploaded file after persistence failed: {}", file.path(), exception);
        }
    }
}
