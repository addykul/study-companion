package dev.studycompanion.api.mapper;

import dev.studycompanion.api.dto.response.ChatResponse;
import dev.studycompanion.api.dto.response.ChatHistoryResponse;
import dev.studycompanion.api.dto.response.ChatMessageResponse;
import dev.studycompanion.api.dto.response.CourseResponse;
import dev.studycompanion.api.dto.response.DocumentResponse;
import dev.studycompanion.api.dto.response.ProgressResponse;
import dev.studycompanion.api.dto.response.QuestionResultResponse;
import dev.studycompanion.api.dto.response.QuizQuestionResponse;
import dev.studycompanion.api.dto.response.QuizResponse;
import dev.studycompanion.api.dto.response.QuizSubmissionResponse;
import dev.studycompanion.api.dto.response.SourceReference;
import dev.studycompanion.api.dto.response.TopicProgressResponse;
import dev.studycompanion.domain.model.ChatAnswer;
import dev.studycompanion.domain.model.ConversationHistory;
import dev.studycompanion.domain.model.Course;
import dev.studycompanion.domain.model.CourseProgress;
import dev.studycompanion.domain.model.StudyDocument;
import dev.studycompanion.domain.model.Quiz;
import dev.studycompanion.domain.model.QuizSubmissionResult;
import java.util.List;
import java.util.Locale;

public final class ApiMapper {
    private ApiMapper() {}

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(course.id(), course.name(), course.description(),
                course.createdAt(), course.documentCount());
    }

    public static DocumentResponse toResponse(StudyDocument document) {
        return new DocumentResponse(document.id(), document.courseId(), document.fileName(),
                document.contentType(), document.status().name(), document.errorMessage(), document.createdAt());
    }

    public static ChatResponse toResponse(ChatAnswer answer) {
        List<SourceReference> references = answer.sources().stream()
                .map(source -> new SourceReference(source.documentId(), source.documentName(),
                        source.page(), source.content()))
                .toList();
        return new ChatResponse(answer.conversationId(), answer.answer(), references);
    }

    public static ChatHistoryResponse toResponse(ConversationHistory history) {
        List<ChatMessageResponse> messages = history.messages().stream()
                .map(message -> new ChatMessageResponse(message.id(), message.role().toLowerCase(Locale.ROOT),
                        message.content(), message.createdAt(), message.sources().stream()
                        .map(source -> new SourceReference(source.documentId(), source.documentName(),
                                source.page(), source.excerpt()))
                        .toList()))
                .toList();
        return new ChatHistoryResponse(history.conversationId(), messages);
    }

    public static QuizResponse toResponse(Quiz quiz) {
        List<QuizQuestionResponse> questions = quiz.questions().stream()
                .map(question -> new QuizQuestionResponse(question.id(), question.topic(),
                        question.prompt(), question.options()))
                .toList();
        return new QuizResponse(quiz.id(), quiz.courseId(), quiz.title(), quiz.topic(), questions, quiz.createdAt());
    }

    public static QuizSubmissionResponse toResponse(QuizSubmissionResult submission) {
        List<QuestionResultResponse> results = submission.outcomes().stream()
                .map(result -> new QuestionResultResponse(result.questionId(), result.correct(),
                        result.selectedIndex(), result.correctIndex(), result.explanation(), result.topic()))
                .toList();
        return new QuizSubmissionResponse(submission.id(), submission.quizId(), submission.score(),
                submission.totalQuestions(), results, submission.createdAt());
    }

    public static ProgressResponse toResponse(CourseProgress progress) {
        List<TopicProgressResponse> topics = progress.topics().stream()
                .map(topic -> new TopicProgressResponse(topic.topic(), topic.correct(), topic.total(), topic.accuracy()))
                .toList();
        return new ProgressResponse(progress.quizzesTaken(), progress.averageScorePercent(),
                topics, progress.reviewSuggestions());
    }
}
