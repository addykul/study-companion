package dev.studycompanion.api.dto.request;

import java.util.Map;
import java.util.UUID;

public record SubmitQuizRequest(Map<UUID, Integer> answers) {}
