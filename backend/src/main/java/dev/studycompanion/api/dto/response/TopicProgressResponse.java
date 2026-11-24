package dev.studycompanion.api.dto.response;

public record TopicProgressResponse(String topic, int correct, int total, double accuracy) {}
