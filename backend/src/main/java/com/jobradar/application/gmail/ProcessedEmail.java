package com.jobradar.application.gmail;

import com.jobradar.application.model.user.User;
import jakarta.persistence.*;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_emails",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "gmailMessageId"})
        }
)
public class ProcessedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gmail message ID is used to prevent duplicate processing
    @Column(nullable = false)
    private String gmailMessageId;

    private String sender;

    private String subject;

    @Column(length = 3000)
    private String snippet;

    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private boolean jobRelated = false;

    @Column(nullable = false)
    private boolean processed = false;

    private LocalDateTime processedAt;

    @ManyToOne(optional = false)
    private User user;


    public Long getId() { return id; }

    public String getGmailMessageId() { return gmailMessageId; }
    public void setGmailMessageId(String gmailMessageId) { this.gmailMessageId = gmailMessageId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject;}

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public boolean isJobRelated() { return jobRelated; }
    public void setJobRelated(boolean jobRelated) { this.jobRelated = jobRelated; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
