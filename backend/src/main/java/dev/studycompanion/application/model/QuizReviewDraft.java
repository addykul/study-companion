package dev.studycompanion.application.model;

import java.util.List;

/** Structured response from the independent, source-grounded quiz review pass. */
public record QuizReviewDraft(List<QuestionReview> questions) {
    public record QuestionReview(int questionNumber, boolean supportedByExactlyOneOption,
                                 int correctIndex, String explanation, String evidenceQuote) {}
}
