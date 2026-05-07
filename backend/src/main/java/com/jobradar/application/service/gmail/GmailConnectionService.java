package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.google.GoogleTokenResponse;
import com.jobradar.application.gmail.GmailConnection;
import com.jobradar.application.gmail.GmailConnectionRepository;
import com.jobradar.application.gmail.GmailOAuthState;
import com.jobradar.application.gmail.GmailOAuthStateRepository;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GmailConnectionService {
    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailOAuthStateRepository gmailOAuthStateRepository;
    private final UserRepository userRepository;

    public GmailConnectionService(GmailConnectionRepository gmailConnectionRepository,
                                  GmailOAuthStateRepository gmailOAuthStateRepository,
                                  UserRepository userRepository) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailOAuthStateRepository = gmailOAuthStateRepository;
        this.userRepository = userRepository;
    }

    public void saveTokens(String state, GoogleTokenResponse tokenResponse){
        GmailOAuthState oAuthState = gmailOAuthStateRepository.findByStateAndUsedFalse(state)
                .orElseThrow(() -> new RuntimeException("Invalid or already used OAuth state"));

        User user = userRepository.findById(oAuthState.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for OAuth state"));

        GmailConnection connection = gmailConnectionRepository.findByUser(user)
                .orElseGet(() -> {
                    GmailConnection newConnection = new GmailConnection();
                    newConnection.setUser(user);
                    return newConnection;
                });
        connection.setConnected(true);
        connection.setAccessToken(tokenResponse.accessToken());
        connection.setRefreshToken(tokenResponse.refreshToken());
        connection.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.expiresIn()));
        connection.setConnectedAt(LocalDateTime.now());
        connection.setDisconnectedAt(null);

        gmailConnectionRepository.save(connection);

        oAuthState.setUsed(true);
        gmailOAuthStateRepository.save(oAuthState);
    }

}
