package com.jobradar.application.controller;

import com.jobradar.application.dto.DocumentResponse;
import com.jobradar.application.model.Document;
import com.jobradar.application.model.DocumentType;
import com.jobradar.application.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument (@PathVariable Long documentId){
        Resource file = documentService.downloadDocument(documentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
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

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
