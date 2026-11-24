package dev.studycompanion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String uploadDir, String frontendOrigin, Retrieval retrieval) {
    public record Retrieval(int topK, double similarityThreshold) {}
}
