package dev.studycompanion.domain.model;

import java.util.UUID;

public record QuestionOutcome(UUID questionId, boolean correct, Integer selectedIndex,
                              Integer correctIndex, String explanation, String topic) {}
