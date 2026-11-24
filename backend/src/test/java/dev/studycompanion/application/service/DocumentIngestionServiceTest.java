package dev.studycompanion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.studycompanion.application.port.StudyAiProvider;
import dev.studycompanion.infrastructure.document.PdfAndTextDocumentExtractor;
import dev.studycompanion.domain.model.DocumentChunk;
import dev.studycompanion.domain.repository.DocumentChunkRepository;
import dev.studycompanion.domain.repository.DocumentRepository;
import dev.studycompanion.infrastructure.persistence.jdbc.PgVectorFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DocumentIngestionServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void splitsLongTextIntoOverlappingChunks() {
        String input = "word ".repeat(500);

        List<String> chunks = new TextChunker().split(input);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.getFirst().length()).isLessThanOrEqualTo(900);
        assertThat(chunks.get(1)).contains(chunks.getFirst().substring(chunks.getFirst().length() - 100));
    }

    @Test
    void formatsEmbeddingAsPostgresVectorLiteral() {
        assertThat(PgVectorFormatter.toLiteral(new float[]{0.25f, -1.5f, 2f}))
                .isEqualTo("[0.25,-1.5,2.0]");
    }

    @Test
    void embedsDocumentChunksInBoundedBatches() throws Exception {
        StudyAiProvider aiProvider = mock(StudyAiProvider.class);
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentChunkRepository chunkRepository = mock(DocumentChunkRepository.class);
        when(aiProvider.embedAll(anyList())).thenAnswer(invocation -> {
            List<String> texts = invocation.getArgument(0);
            return texts.stream().map(ignored -> new float[1536]).toList();
        });
        Path file = tempDir.resolve("notes.txt");
        Files.writeString(file, "word ".repeat(12_000));

        DocumentIngestionService service = new DocumentIngestionService(new PdfAndTextDocumentExtractor(),
                new TextChunker(), documentRepository, chunkRepository, aiProvider);
        service.processAsync(UUID.randomUUID(), UUID.randomUUID(), file, false);

        ArgumentCaptor<List<String>> batches = ArgumentCaptor.forClass(List.class);
        int expectedBatchCount = (new TextChunker().split(Files.readString(file)).size() + 31) / 32;
        verify(aiProvider, times(expectedBatchCount)).embedAll(batches.capture());
        assertThat(batches.getAllValues()).allSatisfy(batch -> assertThat(batch).hasSizeBetween(1, 32));
        assertThat(batches.getAllValues().stream().mapToInt(List::size).sum())
                .isEqualTo(new TextChunker().split(Files.readString(file)).size());
        ArgumentCaptor<List<DocumentChunk>> savedChunks = ArgumentCaptor.forClass(List.class);
        verify(chunkRepository, times(expectedBatchCount)).saveAll(savedChunks.capture());
        verify(documentRepository).updateStatus(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(dev.studycompanion.domain.model.DocumentStatus.READY),
                org.mockito.ArgumentMatchers.isNull());
        assertThat(savedChunks.getAllValues().stream().mapToInt(List::size).sum())
                .isEqualTo(new TextChunker().split(Files.readString(file)).size());
    }
}
