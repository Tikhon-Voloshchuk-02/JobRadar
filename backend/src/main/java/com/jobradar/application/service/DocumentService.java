package com.jobradar.application.service;

import com.jobradar.application.dto.DocumentResponse;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.Document;
import com.jobradar.application.model.DocumentType;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;

    public DocumentService(DocumentRepository documentRepository,
                           ApplicationRepository applicationRepository){
        this.documentRepository=documentRepository;
        this.applicationRepository=applicationRepository;
    }

    public List<Document> getDocumentsRaw(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId);
    }

    @Value("${app.upload-dir}")
    private String uploadDir;


// < ----- upload DOcs ----- >
    public void uploadDocument(Long applicationId, MultipartFile file, DocumentType type){

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found!"));




        // <CREATE DIRECTORY>
        try{

            Files.createDirectories(Paths.get(uploadDir));

        } catch (IOException e){
            throw new RuntimeException("Couldn't create upload directory", e);
        }

        String originalFileName = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID() + " - " + originalFileName;
        Path filePath = Paths.get(uploadDir, storedFileName);


        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }

        Document document = new Document();
        document.setName(storedFileName);
        document.setOriginalFileName(originalFileName);
        document.setType(type);
        document.setFilePath(filePath.toString());
        document.setUploadedAt(LocalDateTime.now());
        document.setApplication(application);

        documentRepository.save(document);
    }

    // < ----- download DOcs ----- >
    public Resource downloadDocument(Long documentId){
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document isn't found =("));

        try {
            Path path = Paths.get(doc.getFilePath());
            Resource res = new UrlResource(path.toUri());

            if(!res.exists() || !res.isReadable()){
                throw new RuntimeException("File not found OR now readable =(");
            }

            return res;

        } catch (MalformedURLException e){
            throw new RuntimeException("FIle download failed", e);
        }
    }

    // < ----- delete DOcs ----- >
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file", e);
        }

        documentRepository.delete(document);
    }

    // < ----- Response DOcs ----- >
    public List<DocumentResponse> getDocuments(Long applicationId){
        return documentRepository.findByApplicationId(applicationId)
                .stream()
                .map(doc -> {
                    DocumentResponse dto = new DocumentResponse();
                    dto.setId(doc.getId());
                    dto.setName(doc.getName());
                    dto.setOriginalFileName(doc.getOriginalFileName());
                    dto.setType(doc.getType());
                    dto.setUploadedAt(doc.getUploadedAt());
                    return dto;
                })
                .toList();
    }
}
