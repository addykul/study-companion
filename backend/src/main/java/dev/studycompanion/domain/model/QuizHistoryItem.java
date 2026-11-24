package dev.studycompanion.domain.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuizHistoryItem(List<QuizQuestion> questions, Map<UUID, Integer> answers,
                              int score, int totalQuestions) {
    public QuizHistoryItem {
        questions = List.copyOf(questions);
        answers = Map.copyOf(answers);
    }
}
