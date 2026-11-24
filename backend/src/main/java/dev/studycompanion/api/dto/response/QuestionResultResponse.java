package dev.studycompanion.api.dto.response;

import java.util.UUID;

public record QuestionResultResponse(UUID questionId, boolean correct, Integer selectedIndex,
                                     Integer correctIndex, String explanation, String topic) {}
