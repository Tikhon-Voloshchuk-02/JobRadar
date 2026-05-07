package com.jobradar.application.gmail;

import com.jobradar.application.model.user.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_emails")
public class ProcessedEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // used to protect against repeated processing.
    @Column(unique = true, nullable = false)
    private String gmailMessageId;

    private String sender;

    private String subject;

    @Column(length = 3000)
    private String snippet;

    private LocalDateTime receivedAt;

    private boolean jobRelated;

    private boolean processed;

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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
