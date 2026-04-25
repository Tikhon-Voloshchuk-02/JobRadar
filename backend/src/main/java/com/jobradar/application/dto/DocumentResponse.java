package com.jobradar.application.dto;

import com.jobradar.application.model.DocumentType;
import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String name;
    private String originalFileName;
    private DocumentType type;
    private LocalDateTime uploadedAt;

    public DocumentResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
