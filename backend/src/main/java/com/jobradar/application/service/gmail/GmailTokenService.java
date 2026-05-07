package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.google.GoogleRefreshTokenResponse;
import com.jobradar.application.gmail.GmailConnection;
import com.jobradar.application.gmail.GmailConnectionRepository;
import com.jobradar.application.model.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
public class GmailTokenService {
    private final GmailConnectionRepository gmailConnectionRepository;
    private final RestClient restClient;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    public GmailTokenService(GmailConnectionRepository gmailConnectionRepository) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.restClient = RestClient.create();
    }

    @Transactional
    public String getValidAccessToken(User user) {
        GmailConnection connection = gmailConnectionRepository
                .findByUserIdAndActiveTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("Gmail is not connected"));

        if (isAccessTokenStillValid(connection)) {
            return connection.getAccessToken();
        }

        return refreshAccessToken(connection);
    }

    private boolean isAccessTokenStillValid(GmailConnection connection) {
        return connection.getAccessToken() != null
                && connection.getExpiresAt() != null
                && connection.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(1));
    }

    private String refreshAccessToken(GmailConnection connection) {
        GoogleRefreshTokenResponse response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .body("client_id=" + googleClientId +
                        "&client_secret=" + googleClientSecret +
                        "&refresh_token=" + connection.getRefreshToken() +
                        "&grant_type=refresh_token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .body(GoogleRefreshTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new RuntimeException("Failed to refresh Gmail access token");
        }

        connection.setAccessToken(response.accessToken());
        connection.setExpiresAt(LocalDateTime.now().plusSeconds(response.expiresIn()));

        gmailConnectionRepository.save(connection);

        return response.accessToken();
    }

}
