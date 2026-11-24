package dev.studycompanion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.studycompanion.api.dto.response.SourceReference;
import dev.studycompanion.api.mapper.ApiMapper;
import dev.studycompanion.application.port.StudyAiProvider;
import dev.studycompanion.config.AppProperties;
import dev.studycompanion.domain.model.ChatAnswer;
import dev.studycompanion.domain.model.RetrievedChunk;
import dev.studycompanion.domain.repository.DocumentChunkRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetrievalServiceTest {
    @Test
    void retrievesCourseScopedChunksAndMapsPageAwareCitations() {
        DocumentChunkRepository chunkRepository = mock(DocumentChunkRepository.class);
        StudyAiProvider aiProvider = mock(StudyAiProvider.class);
        AppProperties config = new AppProperties("/tmp/uploads", "http://localhost:5173",
                new AppProperties.Retrieval(5, 0.25));
        RetrievalService service = new RetrievalService(chunkRepository, aiProvider, config);
        UUID courseId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        List<RetrievedChunk> chunks = List.of(new RetrievedChunk(UUID.randomUUID(), documentId,
                "week-2.pdf", 4, "A source excerpt.", 0.91));
        when(aiProvider.embed("page aware citations")).thenReturn(new float[1536]);
        when(chunkRepository.findSimilarReadyByCourse(eq(courseId), any(float[].class), eq(0.25), eq(5)))
                .thenReturn(chunks);

        List<RetrievedChunk> retrieved = service.retrieve(courseId, "page aware citations");
        List<SourceReference> citations = ApiMapper.toResponse(new ChatAnswer(UUID.randomUUID(), "answer", retrieved)).sources();

        verify(chunkRepository).findSimilarReadyByCourse(eq(courseId), any(float[].class), eq(0.25), eq(5));
        assertThat(citations).containsExactly(new SourceReference(documentId, "week-2.pdf", 4, "A source excerpt."));
    }
}
