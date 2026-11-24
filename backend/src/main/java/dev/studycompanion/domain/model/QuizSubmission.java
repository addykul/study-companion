package dev.studycompanion.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuizSubmission(UUID id, UUID quizId, int score, int totalQuestions,
                            Map<UUID, Integer> answers, Instant createdAt) {
    public QuizSubmission {
        answers = Map.copyOf(answers);
    }
}
