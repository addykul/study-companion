package dev.studycompanion.domain.model;

import java.util.List;
import java.util.UUID;

public record QuizQuestion(UUID id, String topic, String prompt, List<String> options,
                           int correctIndex, String explanation) {
    public QuizQuestion {
        options = List.copyOf(options);
    }
}
