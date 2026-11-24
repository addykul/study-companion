package dev.studycompanion.api.dto.response;

import java.util.List;
import java.util.UUID;

public record QuizQuestionResponse(UUID id, String topic, String prompt, List<String> options) {}
