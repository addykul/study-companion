package dev.studycompanion.api.controller;

import dev.studycompanion.api.dto.request.CreateQuizRequest;
import dev.studycompanion.api.dto.request.SubmitQuizRequest;
import dev.studycompanion.api.dto.response.QuizResponse;
import dev.studycompanion.api.dto.response.QuizSubmissionResponse;
import dev.studycompanion.api.mapper.ApiMapper;
import dev.studycompanion.application.exception.AppException;
import dev.studycompanion.application.service.QuizService;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) { this.quizService = quizService; }

    @PostMapping(value = "/courses/{courseId}/quizzes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public QuizResponse createQuiz(@PathVariable UUID courseId,
                                   @RequestBody(required = false) CreateQuizRequest request) {
        return ApiMapper.toResponse(quizService.create(courseId, request == null ? null : request.topic()));
    }

    @PostMapping("/quizzes/{quizId}/submissions")
    public QuizSubmissionResponse submitQuiz(@PathVariable UUID quizId,
                                             @RequestBody SubmitQuizRequest request) {
        if (request == null) throw AppException.badRequest("Quiz answers are required.");
        return ApiMapper.toResponse(quizService.submit(quizId, request.answers()));
    }
}
