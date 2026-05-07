package com.jobradar.application.gmail;

import com.jobradar.application.model.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gmail_connection")
public class GmailConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private User user;

    private String googleEmail;
    private boolean connected;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;


    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private LocalDateTime expiresAt;

    public Long getId(){return id;}
    public User getUser() {return user;}
    public String getGoogleEmail() {return googleEmail; }

    public boolean isConnected() { return connected; }
    public LocalDateTime getConnectedAt() { return connectedAt; }
    public LocalDateTime getDisconnectedAt() { return disconnectedAt; }

    public void setUser(User user) { this.user = user; }

    public void setGoogleEmail(String googleEmail) { this.googleEmail = googleEmail; }

    public void setConnected(boolean connected) { this.connected = connected; }
    public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }
    public void setDisconnectedAt(LocalDateTime disconnectedAt) { this.disconnectedAt = disconnectedAt; }


    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
