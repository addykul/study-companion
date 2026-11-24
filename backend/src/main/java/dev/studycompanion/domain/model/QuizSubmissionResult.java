package dev.studycompanion.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizSubmissionResult(UUID id, UUID quizId, int score, int totalQuestions,
                                   List<QuestionOutcome> outcomes, Instant createdAt) {
    public QuizSubmissionResult { outcomes = List.copyOf(outcomes); }
}
