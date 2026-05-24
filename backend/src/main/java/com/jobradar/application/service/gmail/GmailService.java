package com.jobradar.application.service.gmail;


import com.jobradar.application.dto.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.dto.gmail.GmailMessageDetailResponse;
import com.jobradar.application.dto.gmail.GmailMessageListResponse;
import com.jobradar.application.model.gmail.GmailConnection;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.repository.gmail.GmailConnectionRepository;
import com.jobradar.application.service.gmail.gmailservice.GmailInboxScanner;

import com.jobradar.application.service.gmail.gmailservice.GmailOAuthUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;


@Service
public class GmailService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailTokenService gmailTokenService;

    private final UserRepository userRepository;

    private final GmailInboxScanner gmailInboxScanner;
    private final GmailOAuthUrlService gmailOAuthUrlService;

    private final RestClient restClient;

    public GmailService(GmailConnectionRepository gmailConnectionRepository,
                        GmailTokenService gmailTokenService,
                        UserRepository userRepository,
                        GmailInboxScanner gmailInboxScanner,
                        GmailOAuthUrlService gmailOAuthUrlService) {

        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailTokenService = gmailTokenService;

        this.userRepository=userRepository;
        this.gmailInboxScanner=gmailInboxScanner;
        this.gmailOAuthUrlService=gmailOAuthUrlService;

        this.restClient = RestClient.create();
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public GmailConnectionStatusResponse getStatus(Authentication auth) {
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

    public String buildGoogleOAuthUrl(Authentication authentication) {
        return gmailOAuthUrlService.buildGoogleOAuthUrl(authentication);
    }

    /**
     * Retrieves recent Gmail messages for the given user.
     * <p>
     * Flow:
     * - obtains a valid access token
     * - refreshes token automatically if expired
     * - calls Gmail API messages.list endpoint
     * - returns message ids and thread ids
     * <p>
     * Used for:
     * - Gmail integration testing
     * - future email processing pipeline
     * - AI email analysis
     */
    public GmailMessageListResponse listMessages(User user) {
        String accessToken = gmailTokenService.getValidAccessToken(user);

        return restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=10")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GmailMessageListResponse.class);
    }

    /**
     * Retrieves full Gmail message details by message id
     * <p>
     * Flow:
     * - obtains a valid access token
     * - calls Gmail API messages.get endpoint
     * - returns message metadata, headers, snippet and payload
     */
    public GmailMessageDetailResponse getMessage(User user, String messageId) {
        String accessToken = gmailTokenService.getValidAccessToken(user);

        return restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GmailMessageDetailResponse.class);
    }

    public void scanAllActiveConnections() {
        gmailInboxScanner.scanAllActiveConnections();
    }


    public GmailConnection setAutoUpdate(Authentication auth, boolean enabled){
        User user = getCurrentUser(auth);

        GmailConnection connection = gmailConnectionRepository.findByUserIdAndConnectedTrue(user.getId())
                                                              .orElseThrow(() -> new RuntimeException("Gmail isn't connected!"));

        connection.setAutoUpdateEnabled(enabled);
        return gmailConnectionRepository.save(connection);
    }

}
