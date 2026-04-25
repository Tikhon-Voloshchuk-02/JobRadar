package com.jobradar.application.controller;

import com.jobradar.application.dto.DocumentResponse;
import com.jobradar.application.model.Document;
import com.jobradar.application.model.DocumentType;
import com.jobradar.application.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/applications/{applicationId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments( @PathVariable Long applicationId) {
        return ResponseEntity.ok(documentService.getDocuments(applicationId));
    }

    @PostMapping("/applications/{applicationId}/documents")
    public ResponseEntity<Void> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type
    ) {
        documentService.uploadDocument(applicationId, file, type);
        return ResponseEntity.ok().build();
    }
}
