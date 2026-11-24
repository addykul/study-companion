package dev.studycompanion.application.port;

import dev.studycompanion.domain.model.DocumentPage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Outbound port for extracting page-aware text from an uploaded file. */
public interface DocumentTextExtractor {
    List<DocumentPage> extract(Path file, boolean pdf) throws IOException;
}
