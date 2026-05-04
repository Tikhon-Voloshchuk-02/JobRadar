package com.jobradar.application.model.ai;

import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_suggestions")
public class AiSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus suggestedStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfidenceLevel confidence;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(length = 2000)
    private String emailSnippet;

    @Column(nullable = false)
    private String source = "AI_GMAIL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus suggestionStatus = SuggestionStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    public AiSuggestion() {
    }

    public Long getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ApplicationStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public ApplicationStatus getSuggestedStatus() {
        return suggestedStatus;
    }

    public void setSuggestedStatus(ApplicationStatus suggestedStatus) {
        this.suggestedStatus = suggestedStatus;
    }

    public ConfidenceLevel getConfidence() {
        return confidence;
    }

    public void setConfidence(ConfidenceLevel confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEmailSnippet() {
        return emailSnippet;
    }

    public void setEmailSnippet(String emailSnippet) {
        this.emailSnippet = emailSnippet;
    }

    public String getSource() {
        return source;
    }

    public SuggestionStatus getSuggestionStatus() {
        return suggestionStatus;
    }

    public void setSuggestionStatus(SuggestionStatus suggestionStatus) {
        this.suggestionStatus = suggestionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
