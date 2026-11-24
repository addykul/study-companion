package dev.studycompanion.api.controller;

import dev.studycompanion.api.dto.response.ProgressResponse;
import dev.studycompanion.api.mapper.ApiMapper;
import dev.studycompanion.application.service.QuizService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/progress")
public class ProgressController {
    private final QuizService quizService;

    public ProgressController(QuizService quizService) { this.quizService = quizService; }

    @GetMapping
    public ProgressResponse progress(@PathVariable UUID courseId) {
        return ApiMapper.toResponse(quizService.progress(courseId));
    }
}
