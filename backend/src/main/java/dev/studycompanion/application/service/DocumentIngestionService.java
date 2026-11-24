package dev.studycompanion.application.service;

import dev.studycompanion.application.port.StudyAiProvider;
import dev.studycompanion.application.port.DocumentTextExtractor;
import dev.studycompanion.domain.model.DocumentChunk;
import dev.studycompanion.domain.model.DocumentPage;
import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.domain.model.StudyDocument;
import dev.studycompanion.domain.repository.DocumentChunkRepository;
import dev.studycompanion.domain.repository.DocumentRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DocumentIngestionService {
    private static final int EMBEDDING_BATCH_SIZE = 32;
    private static final int EXPECTED_VECTOR_DIMENSIONS = 1536;
    private static final Logger logger = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final DocumentTextExtractor textExtractor;
    private final TextChunker textChunker;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final StudyAiProvider aiProvider;

    public DocumentIngestionService(DocumentTextExtractor textExtractor, TextChunker textChunker,
                                    DocumentRepository documentRepository,
                                    DocumentChunkRepository chunkRepository, StudyAiProvider aiProvider) {
        this.textExtractor = textExtractor;
        this.textChunker = textChunker;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.aiProvider = aiProvider;
    }

    @Async("documentIngestionExecutor")
    public void processAsync(UUID documentId, UUID courseId, Path file, boolean pdf) {
        try {
            List<PendingChunk> chunks = extractChunks(file, pdf);
            if (chunks.isEmpty()) throw new IllegalArgumentException("No readable text was found in this file.");
            embedAndStore(documentId, courseId, chunks);
            documentRepository.updateStatus(documentId, DocumentStatus.READY, null);
        } catch (Exception exception) {
            markFailed(documentId, exception);
        }
    }

    private List<PendingChunk> extractChunks(Path file, boolean pdf) throws Exception {
        List<DocumentPage> pages = textExtractor.extract(file, pdf);
        return pages.stream().flatMap(page -> textChunker.split(page.text()).stream()
                .filter(text -> !text.isBlank())
                .map(text -> new PendingChunk(page.pageNumber(), text))
        ).toList();
    }

    private void embedAndStore(UUID documentId, UUID courseId, List<PendingChunk> chunks) {
        for (int batchStart = 0; batchStart < chunks.size(); batchStart += EMBEDDING_BATCH_SIZE) {
            List<PendingChunk> batch = chunks.subList(batchStart,
                    Math.min(batchStart + EMBEDDING_BATCH_SIZE, chunks.size()));
            List<float[]> embeddings = aiProvider.embedAll(batch.stream().map(PendingChunk::content).toList());
            validateEmbeddings(batch, embeddings);

            int firstChunkIndex = batchStart;
            List<DocumentChunk> entities = java.util.stream.IntStream.range(0, batch.size())
                    .mapToObj(index -> toDocumentChunk(documentId, courseId, firstChunkIndex + index,
                            batch.get(index), embeddings.get(index)))
                    .toList();
            chunkRepository.saveAll(entities);
        }
    }

    private void validateEmbeddings(List<PendingChunk> chunks, List<float[]> embeddings) {
        if (embeddings == null || embeddings.size() != chunks.size()) {
            throw new IllegalStateException("The embedding provider returned an unexpected number of vectors.");
        }
        if (embeddings.stream().anyMatch(vector -> vector == null || vector.length != EXPECTED_VECTOR_DIMENSIONS)) {
            throw new IllegalStateException("The configured embedding model must return 1,536 dimensions.");
        }
    }

    private DocumentChunk toDocumentChunk(UUID documentId, UUID courseId, int index,
                                          PendingChunk chunk, float[] embedding) {
        return new DocumentChunk(UUID.randomUUID(), courseId, documentId, chunk.pageNumber(), index,
                chunk.content(), embedding);
    }

    private void markFailed(UUID documentId, Exception exception) {
        logger.warn("Document ingestion failed for document {}", documentId, exception);
        String message = exception.getMessage() == null ? "Document processing failed." : exception.getMessage();
        String userMessage = message.length() > 490 ? message.substring(0, 490) : message;
        chunkRepository.deleteByDocumentId(documentId);
        documentRepository.updateStatus(documentId, DocumentStatus.FAILED, userMessage);
    }

    private record PendingChunk(Integer pageNumber, String content) {}
}
