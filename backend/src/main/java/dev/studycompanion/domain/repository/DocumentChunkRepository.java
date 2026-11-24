package dev.studycompanion.domain.repository;

import dev.studycompanion.domain.model.DocumentChunk;
import dev.studycompanion.domain.model.RetrievedChunk;
import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository {
    void saveAll(List<DocumentChunk> chunks);
    void deleteByDocumentId(UUID documentId);
    List<RetrievedChunk> findSimilarReadyByCourse(UUID courseId, float[] queryEmbedding,
                                                   double similarityThreshold, int limit);
}
