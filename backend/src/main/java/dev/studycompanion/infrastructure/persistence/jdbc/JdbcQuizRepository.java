package dev.studycompanion.infrastructure.persistence.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.studycompanion.domain.model.Quiz;
import dev.studycompanion.domain.model.QuizHistoryItem;
import dev.studycompanion.domain.model.QuizQuestion;
import dev.studycompanion.domain.model.QuizSubmission;
import dev.studycompanion.domain.repository.QuizRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcQuizRepository implements QuizRepository {
    private static final TypeReference<List<QuizQuestion>> QUESTIONS_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<UUID, Integer>> ANSWERS_TYPE = new TypeReference<>() {};
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcQuizRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Quiz save(Quiz quiz) {
        Timestamp createdAt = jdbcTemplate.queryForObject("""
                INSERT INTO quizzes(id, course_id, title, topic, questions_json)
                VALUES (?, ?, ?, ?, ?::jsonb) RETURNING created_at
                """, Timestamp.class, quiz.id(), quiz.courseId(), quiz.title(), quiz.topic(),
                writeJson(quiz.questions()));
        return new Quiz(quiz.id(), quiz.courseId(), quiz.title(), quiz.topic(), quiz.questions(), createdAt.toInstant());
    }

    @Override
    public Optional<Quiz> findById(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, course_id, title, topic, questions_json::text AS questions_json, created_at
                FROM quizzes WHERE id = ?
                """, this::mapQuiz, id).stream().findFirst();
    }

    @Override
    public QuizSubmission saveSubmission(QuizSubmission submission) {
        Timestamp createdAt = jdbcTemplate.queryForObject("""
                INSERT INTO quiz_submissions(id, quiz_id, score, total_questions, answers_json)
                VALUES (?, ?, ?, ?, ?::jsonb) RETURNING created_at
                """, Timestamp.class, submission.id(), submission.quizId(), submission.score(),
                submission.totalQuestions(), writeJson(submission.answers()));
        return new QuizSubmission(submission.id(), submission.quizId(), submission.score(),
                submission.totalQuestions(), submission.answers(), createdAt.toInstant());
    }

    @Override
    public List<QuizHistoryItem> findHistoryByCourseId(UUID courseId) {
        return jdbcTemplate.query("""
                SELECT q.questions_json::text AS questions_json, s.answers_json::text AS answers_json,
                       s.score, s.total_questions
                FROM quizzes q JOIN quiz_submissions s ON s.quiz_id = q.id
                WHERE q.course_id = ? ORDER BY s.created_at DESC
                """, (rs, rowNum) -> new QuizHistoryItem(
                readJson(rs.getString("questions_json"), QUESTIONS_TYPE),
                readJson(rs.getString("answers_json"), ANSWERS_TYPE),
                rs.getInt("score"), rs.getInt("total_questions")), courseId);
    }

    private Quiz mapQuiz(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Quiz(rs.getObject("id", UUID.class), rs.getObject("course_id", UUID.class),
                rs.getString("title"), rs.getString("topic"),
                readJson(rs.getString("questions_json"), QUESTIONS_TYPE),
                rs.getTimestamp("created_at").toInstant());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize quiz data for persistence.", exception);
        }
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored quiz data is invalid.", exception);
        }
    }
}
