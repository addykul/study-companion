package dev.studycompanion.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Quiz(UUID id, UUID courseId, String title, String topic,
                   List<QuizQuestion> questions, Instant createdAt) {
    public Quiz {
        questions = List.copyOf(questions);
    }
}
