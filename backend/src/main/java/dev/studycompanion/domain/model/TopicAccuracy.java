package dev.studycompanion.domain.model;

public record TopicAccuracy(String topic, int correct, int total, double accuracy) {}
