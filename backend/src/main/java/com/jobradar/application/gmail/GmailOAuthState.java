package com.jobradar.application.gmail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class GmailOAuthState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String state;

    private Long userId;

    private LocalDateTime createdAt;

    private boolean used = false;

    public GmailOAuthState() {}

    public Long getId() { return id; }

    public String getState() {return state; }
    public void setState(String state) { this.state = state; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() {return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isUsed() {return used; }

    public void setUsed(boolean used) { this.used = used; }

}
