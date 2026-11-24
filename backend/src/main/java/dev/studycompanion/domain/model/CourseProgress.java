package dev.studycompanion.domain.model;

import java.util.List;

public record CourseProgress(int quizzesTaken, int averageScorePercent,
                             List<TopicAccuracy> topics, List<String> reviewSuggestions) {
    public CourseProgress {
        topics = List.copyOf(topics);
        reviewSuggestions = List.copyOf(reviewSuggestions);
    }
}
