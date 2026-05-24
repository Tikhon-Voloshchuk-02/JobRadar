package com.jobradar.application.service.gmail.gmailservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.model.gmail.GmailConnection;
import com.jobradar.application.repository.gmail.GmailConnectionRepository;
import com.jobradar.application.service.gmail.GmailEmailProcessingService;
import com.jobradar.application.service.gmail.GmailTokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GmailInboxScanner {

    private static final Logger log = LoggerFactory.getLogger(GmailInboxScanner.class);

    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailTokenService gmailTokenService;
    private final GmailEmailProcessingService gmailEmailProcessingService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GmailInboxScanner(GmailConnectionRepository gmailConnectionRepository,
                             GmailTokenService gmailTokenService,
                             GmailEmailProcessingService gmailEmailProcessingService) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailTokenService = gmailTokenService;
        this.gmailEmailProcessingService = gmailEmailProcessingService;
        this.restClient = RestClient.create();
        this.objectMapper = new ObjectMapper();
    }

    public void scanAllActiveConnections() {
        List<GmailConnection> connections =
                gmailConnectionRepository.findByConnectedTrue();

        log.info("Gmail auto scan tick. Connected accounts: {}", connections.size());

        for (GmailConnection connection : connections) {
            try {
                processConnection(connection);
            } catch (Exception e) {
                log.warn("Failed to process Gmail connection id={}", connection.getId(), e);
            }
        }
    }

    private void processConnection(GmailConnection connection) throws Exception {
        log.info("Processing Gmail connection id={}, email={}",
                connection.getId(),
                connection.getGoogleEmail()
        );

        String accessToken =
                gmailTokenService.getValidAccessToken(connection.getUser());

        String response = restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=5&q=is:unread")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);

        if (!root.has("messages") || root.get("messages").isEmpty()) {
            log.debug("No unread Gmail messages found");
            return;
        }

        String messageId = root.get("messages")
                .get(0)
                .get("id")
                .asText();

        gmailEmailProcessingService.processSingleEmail(
                connection.getUser(),
                messageId,
                accessToken
        );

        markAsRead(accessToken, messageId);
    }

    private void markAsRead(String accessToken, String messageId) {
        restClient.post()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId + "/modify")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body("""
                    {
                      "removeLabelIds": ["UNREAD"]
                    }
                    """)
                .retrieve()
                .body(String.class);

        log.info("Marked Gmail message as read: {}", messageId);
    }
}
