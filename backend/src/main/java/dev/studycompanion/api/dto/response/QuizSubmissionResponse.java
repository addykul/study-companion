package dev.studycompanion.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizSubmissionResponse(UUID id, UUID quizId, int score, int totalQuestions,
                                    List<QuestionResultResponse> results, Instant createdAt) {}
