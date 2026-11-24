package dev.studycompanion.domain.model;

import java.util.List;

/** Structured response shape requested from the language model. */
public record QuizDraft(List<Question> questions) {
    public record Question(String topic, String prompt, List<String> options,
                          int correctIndex, String explanation) {}
}
