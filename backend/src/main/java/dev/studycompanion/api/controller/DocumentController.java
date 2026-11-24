package dev.studycompanion.api.controller;

import dev.studycompanion.api.dto.response.DocumentResponse;
import dev.studycompanion.api.mapper.ApiMapper;
import dev.studycompanion.application.service.DocumentService;
import dev.studycompanion.application.exception.AppException;
import dev.studycompanion.application.model.DocumentUploadCommand;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) { this.documentService = documentService; }

    @PostMapping(value = "/courses/{courseId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse uploadDocument(@PathVariable UUID courseId,
                                           @RequestParam("file") MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            return ApiMapper.toResponse(documentService.upload(courseId,
                    new DocumentUploadCommand(file.getOriginalFilename(), content)));
        } catch (IOException exception) {
            throw AppException.badRequest("Could not read the uploaded file. Please try again.");
        }
    }

    @GetMapping("/courses/{courseId}/documents")
    public List<DocumentResponse> listDocuments(@PathVariable UUID courseId) {
        return documentService.list(courseId).stream().map(ApiMapper::toResponse).toList();
    }

    @GetMapping("/documents/{documentId}")
    public DocumentResponse getDocument(@PathVariable UUID documentId) {
        return ApiMapper.toResponse(documentService.get(documentId));
    }
}
