package dev.studycompanion.api.controller;

import dev.studycompanion.api.dto.request.ChatRequest;
import dev.studycompanion.api.dto.response.ChatResponse;
import dev.studycompanion.api.mapper.ApiMapper;
import dev.studycompanion.application.exception.AppException;
import dev.studycompanion.application.service.ChatService;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) { this.chatService = chatService; }

    @PostMapping
    public ChatResponse ask(@PathVariable UUID courseId, @RequestBody ChatRequest request) {
        if (request == null) throw AppException.badRequest("Chat details are required.");
        return ApiMapper.toResponse(chatService.ask(courseId, request.conversationId(), request.question()));
    }
}
