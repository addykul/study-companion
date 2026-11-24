package dev.studycompanion.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizResponse(UUID id, UUID courseId, String title, String topic,
                           List<QuizQuestionResponse> questions, Instant createdAt) {}
