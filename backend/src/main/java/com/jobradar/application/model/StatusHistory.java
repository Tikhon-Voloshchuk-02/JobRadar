package com.jobradar.application.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus newStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    public StatusHistory() { }

    public StatusHistory(ApplicationStatus oldStatus, ApplicationStatus newStatus, Application application) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.application = application;
    }

    @PrePersist
    protected void onCreate() { this.changedAt = LocalDateTime.now(); }

    public Long getId() { return id; }

    public ApplicationStatus getOldStatus() { return oldStatus; }

    public void setOldStatus(ApplicationStatus oldStatus) { this.oldStatus = oldStatus; }

    public ApplicationStatus getNewStatus() { return newStatus; }

    public void setNewStatus(ApplicationStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }

    public Application getApplication() { return application; }

    public void setApplication(Application application) { this.application = application; }
}
