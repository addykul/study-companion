package dev.studycompanion.application.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TextChunker {
    private static final int CHUNK_SIZE = 900;
    private static final int OVERLAP = 150;

    public List<String> split(String text) {
        String normalized = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            if (end < normalized.length()) {
                int boundary = normalized.lastIndexOf(' ', end);
                if (boundary > start + CHUNK_SIZE / 2) end = boundary;
            }
            chunks.add(normalized.substring(start, end).trim());
            if (end >= normalized.length()) break;
            start = Math.max(start + 1, end - OVERLAP);
        }
        return chunks;
    }
}
