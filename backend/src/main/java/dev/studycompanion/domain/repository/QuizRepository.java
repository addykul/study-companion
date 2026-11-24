package dev.studycompanion.domain.repository;

import dev.studycompanion.domain.model.Quiz;
import dev.studycompanion.domain.model.QuizHistoryItem;
import dev.studycompanion.domain.model.QuizSubmission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizRepository {
    Quiz save(Quiz quiz);
    Optional<Quiz> findById(UUID id);
    QuizSubmission saveSubmission(QuizSubmission submission);
    List<QuizHistoryItem> findHistoryByCourseId(UUID courseId);
}
