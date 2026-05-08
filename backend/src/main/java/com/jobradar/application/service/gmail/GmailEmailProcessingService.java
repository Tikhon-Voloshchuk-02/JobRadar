package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.gmail.GmailConnection;
import com.jobradar.application.gmail.GmailConnectionRepository;

import com.jobradar.application.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.jobradar.application.model.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GmailEmailProcessingService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailTokenService gmailTokenService;
    private final UserRepository userRepository;
    private final RestClient restClient;

    

    public GmailEmailProcessingService(GmailConnectionRepository gmailConnectionRepository,
                                       GmailTokenService gmailTokenService,
                                       UserRepository userRepository) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailTokenService = gmailTokenService;
        this.userRepository = userRepository;
        this.restClient = RestClient.create();
    }


    /**
     * Fetches recent emails from the user's Gmail account using Gmail API.
     *
     * Workflow:
     * 1. Resolve current authenticated user
     * 2. Verify Gmail connection exists
     * 3. Obtain valid OAuth access token
     * 4. Request recent Gmail messages
     * 5. Load full message details for each message
     * 6. Extract useful fields (subject, sender, snippet, received date)
     * 7. Convert messages into GmailMessageDto objects
     *
     * @param auth current authenticated user
     * @return list of recent Gmail messages
     */
    public List<GmailMessageDto> fetchRecentEmails(Authentication auth) {
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GmailConnection connection = gmailConnectionRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Gmail is not connected"));

        String accessToken = gmailTokenService.getValidAccessToken(user);

        String url = "https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=10";

        Map response = restClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> messages =
                (List<Map<String, Object>>) response.getOrDefault("messages", List.of());

        List<GmailMessageDto> result = new ArrayList<>();

        for (Map<String, Object> message : messages) {
            String messageId = (String) message.get("id");

            Map fullMessage = restClient.get()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            String subject = extractHeader(fullMessage, "Subject");
            String from = extractHeader(fullMessage, "From");
            String snippet = (String) fullMessage.get("snippet");

            String internalDateRaw = (String) fullMessage.get("internalDate");
            Instant receivedAt = Instant.ofEpochMilli(Long.parseLong(internalDateRaw));

            result.add(new GmailMessageDto(
                    messageId,
                    subject,
                    from,
                    snippet,
                    receivedAt
            ));
        }

        return result;
    }


    /**
     * Extracts a specific header value from a Gmail API message payload.
     *
     * Example headers:
     * - Subject
     * - From
     * - To
     * - Date
     *
     * @param fullMessage full Gmail API message response
     * @param headerName target header name
     * @return  - header value or null if not found
     */
    private String extractHeader(Map fullMessage, String headerName) {
        Map payload = (Map) fullMessage.get("payload");

        if (payload == null) {
            return null;
        }

        List<Map<String, String>> headers =
                (List<Map<String, String>>) payload.get("headers");

        if (headers == null) {
            return null;
        }

        return headers.stream()
                .filter(header ->
                        headerName.equalsIgnoreCase(header.get("name")))
                .map(header -> header.get("value"))
                .findFirst()
                .orElse(null);
    }
}
