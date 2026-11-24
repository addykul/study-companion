package dev.studycompanion.api.controller;

import dev.studycompanion.api.dto.request.CreateCourseRequest;
import dev.studycompanion.api.dto.response.CourseResponse;
import dev.studycompanion.api.mapper.ApiMapper;
import dev.studycompanion.application.exception.AppException;
import dev.studycompanion.application.service.CourseService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) { this.courseService = courseService; }

    @PostMapping
    public CourseResponse createCourse(@RequestBody CreateCourseRequest request) {
        if (request == null) throw AppException.badRequest("Course details are required.");
        return ApiMapper.toResponse(courseService.create(request.name(), request.description()));
    }

    @GetMapping
    public List<CourseResponse> listCourses() {
        return courseService.list().stream().map(ApiMapper::toResponse).toList();
    }
}
