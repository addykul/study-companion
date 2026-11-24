package dev.studycompanion.infrastructure.document;

import dev.studycompanion.application.port.DocumentTextExtractor;
import dev.studycompanion.domain.model.DocumentPage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfAndTextDocumentExtractor implements DocumentTextExtractor {
    @Override
    public List<DocumentPage> extract(Path file, boolean pdf) throws IOException {
        return pdf ? extractPdf(file) : List.of(new DocumentPage(null, Files.readString(file, StandardCharsets.UTF_8)));
    }

    private List<DocumentPage> extractPdf(Path file) throws IOException {
        List<DocumentPage> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int pageNumber = 1; pageNumber <= document.getNumberOfPages(); pageNumber++) {
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                pages.add(new DocumentPage(pageNumber, stripper.getText(document)));
            }
        }
        return pages;
    }
}
