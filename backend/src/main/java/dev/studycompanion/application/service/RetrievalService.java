package dev.studycompanion.application.service;

import dev.studycompanion.application.port.StudyAiProvider;
import dev.studycompanion.domain.model.RetrievedChunk;
import dev.studycompanion.domain.repository.DocumentChunkRepository;
import dev.studycompanion.config.AppProperties;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RetrievalService {
    private static final int EXPECTED_VECTOR_DIMENSIONS = 1536;

    private final DocumentChunkRepository chunkRepository;
    private final StudyAiProvider aiProvider;
    private final int topK;
    private final double similarityThreshold;

    public RetrievalService(DocumentChunkRepository chunkRepository, StudyAiProvider aiProvider,
                           AppProperties appProperties) {
        this.chunkRepository = chunkRepository;
        this.aiProvider = aiProvider;
        this.topK = appProperties.retrieval().topK();
        this.similarityThreshold = appProperties.retrieval().similarityThreshold();
    }

    public List<RetrievedChunk> retrieve(UUID courseId, String query) {
        float[] queryEmbedding = aiProvider.embed(query);
        if (queryEmbedding == null || queryEmbedding.length != EXPECTED_VECTOR_DIMENSIONS) {
            throw new IllegalStateException("The configured embedding model must return 1,536 dimensions.");
        }
        return chunkRepository.findSimilarReadyByCourse(courseId, queryEmbedding, similarityThreshold, topK);
    }
}
