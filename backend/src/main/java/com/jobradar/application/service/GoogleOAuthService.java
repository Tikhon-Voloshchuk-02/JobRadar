package com.jobradar.application.service;

import com.jobradar.application.dto.google.GoogleTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleOAuthService {
    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final RestClient restClient = RestClient.create();


    /**
     * Exchanges Google OAuth authorization code for access and refresh tokens.
     *
     * This method sends a POST request to Google's OAuth token endpoint
     * and converts the received authorization code into:
     *
     * - access_token  -> used to access Gmail API
     * - refresh_token -> used to refresh expired access tokens
     * - expires_in    -> access token lifetime in seconds
     *
     * The authorization code is received after the user successfully
     * authenticates and grants Gmail access permissions via Google OAuth.
     *
     * OAuth Flow:
     * User Login -> Google Authorization Code -> Token Exchange -> Gmail API Access
     *
     * Google Endpoint:
     * https://oauth2.googleapis.com/token
     */
    public GoogleTokenResponse exchangeCodeForTokens(String code) {

        String body =
                "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                        + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                        + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                        + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                        + "&grant_type=authorization_code";

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }
}
