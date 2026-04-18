package com.jobradar.application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jobradar.application.model.user.User;
import jakarta.persistence.*;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String position;

    private String link;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDate appliedAt;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;


    public Application(){ }

    public Application(String company, String position, String link, String notes,
                       ApplicationStatus status, LocalDateTime createdAt, LocalDate appliedAt) {
        this.company = company;
        this.position = position;
        this.link = link;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.appliedAt = appliedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if(this.status==null){
            this.status=ApplicationStatus.SAVED;
        }
    }
    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }

    public String getCompany() { return company; }

    public void setCompany(String company) { this.company = company; }

    public String getPosition() { return position; }

    public void setPosition(String position) { this.position = position; }

    public String getLink() { return link; }

    public void setLink(String link) { this.link = link; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public ApplicationStatus getStatus() { return status; }

    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDate getAppliedAt() { return appliedAt; }

    public void setAppliedAt(LocalDate appliedAt) { this.appliedAt = appliedAt; }

//  < ------ USERS ------>

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

}
