package com.jobradar.application.service;

import com.jobradar.application.gmail.GmailConnection;
import com.jobradar.application.gmail.GmailConnectionRepository;
import com.jobradar.application.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GmailService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final UserRepository userRepository;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    public GmailService(GmailConnectionRepository gmailConnectionRepository,
                        UserRepository userRepository) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public GmailConnectionStatusResponse getStatus(Authentication auth){
        User user = getCurrentUser(auth);

        return gmailConnectionRepository.findByUser(user)
                .map(connection -> new GmailConnectionStatusResponse(
                        connection.isConnected(),
                        connection.getGoogleEmail(),
                        connection.getConnectedAt()
                ))
                .orElse(new GmailConnectionStatusResponse(false, null, null));
    }

    public String getConnectUrl(Authentication auth) {
        getCurrentUser(auth);


        return "/api/gmail/oauth/google";
    }

    public void disconnect(Authentication auth) {
        User user = getCurrentUser(auth);

        gmailConnectionRepository.findByUser(user).ifPresent(connection -> {
            connection.setConnected(false);
            connection.setDisconnectedAt(LocalDateTime.now());
            gmailConnectionRepository.save(connection);
        });
    }
    /**
     * Mock Gmail connection for development/testing.

     * - gets the current user
     * - searches for an existing GmailConnection
     * - either creates a new
     one * - marks Gmail as connected
     * - saves the connection date
     *
     * Used for:
     * - backend flow testing
     * - UserPage checks
     * - frontend integration
     * - AI pipeline development before Gmail API connection
     */
    public GmailConnectionStatusResponse mockConnect(Authentication auth) {
        User user = getCurrentUser(auth);

        GmailConnection connection = gmailConnectionRepository.findByUser(user)
                .orElseGet(() -> {
                    GmailConnection newConnection = new GmailConnection();
                    newConnection.setUser(user);
                    return newConnection;
                });

        connection.setConnected(true);
        connection.setGoogleEmail(user.getEmail());
        connection.setConnectedAt(LocalDateTime.now());
        connection.setDisconnectedAt(null);

        GmailConnection saved = gmailConnectionRepository.save(connection);

        return new GmailConnectionStatusResponse(
                saved.isConnected(),
                saved.getGoogleEmail(),
                saved.getConnectedAt()
        );
    }

    public String buildGoogleOAuthUrl() {

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=http://localhost:8080/api/gmail/oauth/callback"
                + "&response_type=code"
                + "&scope="
                + "openid%20email%20profile%20https://www.googleapis.com/auth/gmail.readonly"
                + "&access_type=offline"
                + "&prompt=consent";
    }

}
