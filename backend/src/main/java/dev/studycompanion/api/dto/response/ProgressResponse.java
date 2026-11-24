package dev.studycompanion.api.dto.response;

import java.util.List;

public record ProgressResponse(int quizzesTaken, int averageScorePercent,
                               List<TopicProgressResponse> topics, List<String> reviewSuggestions) {}
