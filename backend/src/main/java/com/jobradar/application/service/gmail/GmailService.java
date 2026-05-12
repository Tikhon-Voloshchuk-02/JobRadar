package com.jobradar.application.service.gmail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.dto.gmail.GmailMessageDetailResponse;
import com.jobradar.application.dto.gmail.GmailMessageListResponse;
import com.jobradar.application.gmail.*;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GmailService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailOAuthStateRepository gmailOAuthStateRepository;
    private final UserRepository userRepository;
    private final GmailTokenService gmailTokenService;
    private final ProcessedEmailRepository processedEmailRepository;

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    public GmailService(GmailConnectionRepository gmailConnectionRepository,
                         GmailOAuthStateRepository gmailOAuthStateRepository,
                         UserRepository userRepository,
                         GmailTokenService gmailTokenService,
                         ProcessedEmailRepository processedEmailRepository) {

        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailOAuthStateRepository = gmailOAuthStateRepository;
        this.userRepository = userRepository;
        this.gmailTokenService = gmailTokenService;
        this.processedEmailRepository = processedEmailRepository;

        this.restClient = RestClient.create();
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
     * - marks Gmail as connected
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

    //Marks Gmail message as processed
    public void markMessageAsProcessed(User user,
                                       String gmailMessageId,
                                       boolean jobRelated,
                                       String sender,
                                       String subject,
                                       String snippet) {

        if (isMessageAlreadyProcessed(user, gmailMessageId)) { return; }

        ProcessedEmail processedEmail = new ProcessedEmail();

        processedEmail.setUser(user);
        processedEmail.setGmailMessageId(gmailMessageId);

        processedEmail.setJobRelated(jobRelated);
        processedEmail.setProcessed(true);

        processedEmail.setSender(sender);
        processedEmail.setSubject(subject);
        processedEmail.setSnippet(snippet);

        processedEmail.setProcessedAt(LocalDateTime.now());

        processedEmailRepository.save(processedEmail);
    }

    public boolean isMessageAlreadyProcessed(User user, String gmailMessageId) {
        return processedEmailRepository.existsByUserAndGmailMessageId(user, gmailMessageId);
    }


    /**
     * Creates a temporary OAuth state for the current authenticated user.
     *
     * The state is used to:
     * - protect against CSRF attacks during OAuth flow
     * - bind the Google OAuth callback to a specific user
     * - validate that the callback belongs to the original request
     *
     * Flow:
     * - generate random UUID state
     * - store state in database
     * - bind state to current user
     * - mark state as unused
     *
     * Returns: generated OAuth state string
     */
    public String createOAuthStateForCurrentUser(Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        GmailOAuthState oauthState = new GmailOAuthState();

        oauthState.setState(UUID.randomUUID().toString());
        oauthState.setUserId(currentUser.getId());
        oauthState.setCreatedAt(LocalDateTime.now());
        oauthState.setUsed(false);

        gmailOAuthStateRepository.save(oauthState);

        return oauthState.getState();
    }

    /**
     * Builds Google OAuth URL for Gmail connection.
     *
     * The URL:
     * - requests Gmail readonly access
     * - requests offline access (refresh token)
     * - includes OAuth state for security validation
     * - redirects back to backend callback endpoint
     *
     * Scopes:
     * - openid
     * - email
     * - profile
     * - gmail.readonly
     *
     * Returns:
     * - complete Google OAuth authorization URL
     */
    public String buildGoogleOAuthUrl(Authentication authentication) {

        String state = createOAuthStateForCurrentUser(authentication);

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=http://localhost:8080/api/gmail/oauth/callback"
                + "&response_type=code"
                + "&scope="
                + "openid%20email%20profile%20https://www.googleapis.com/auth/gmail.modify"
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + state;
    }

    /**
     * Retrieves recent Gmail messages for the given user.
     *
     * Flow:
     * - obtains a valid access token
     * - refreshes token automatically if expired
     * - calls Gmail API messages.list endpoint
     * - returns message ids and thread ids
     *
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
     *
     * Flow:
     * - obtains a valid access token
     * - calls Gmail API messages.get endpoint
     * - returns message metadata, headers, snippet and payload
     *
     */
    public GmailMessageDetailResponse getMessage(User user, String messageId){
        String accessToken = gmailTokenService.getValidAccessToken(user);

        return restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GmailMessageDetailResponse.class);
    }


    public void scanAllActiveConnections() {
        List<GmailConnection> connections =
                gmailConnectionRepository.findByConnectedTrue();

        System.out.println("Gmail auto scan tick. Connected accounts: " + connections.size());

        for (GmailConnection connection : connections) {
            try {
                processConnection(connection);
            } catch (Exception e) {
                System.out.println("Failed to process Gmail connection id=" + connection.getId());
                e.printStackTrace();
            }
        }
    }

    private void processConnection(GmailConnection connection) throws Exception {

        System.out.println(
                "Processing Gmail connection id=" + connection.getId()
                        + ", email=" + connection.getGoogleEmail()
        );

        String accessToken =
                gmailTokenService.getValidAccessToken(connection.getUser());

        System.out.println(
                "Access token received for connection id=" + connection.getId()
        );

        String response = restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=5&q=is:unread")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);

        System.out.println(response);

        JsonNode root = objectMapper.readTree(response);

        if (!root.has("messages") || root.get("messages").isEmpty()) {
            System.out.println("No Gmail messages found");
            return;
        }

        String messageId = root.get("messages")
                .get(0)
                .get("id")
                .asText();

        String messageResponse = restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);

        JsonNode messageRoot = objectMapper.readTree(messageResponse);

        String snippet = messageRoot.path("snippet").asText("");
        String subject = extractHeader(messageRoot, "Subject");
        String from = extractHeader(messageRoot, "From");

        System.out.println("Message details received for id=" + messageId);
        System.out.println("From: " + from);
        System.out.println("Subject: " + subject);
        System.out.println("Snippet: " + snippet);

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

        System.out.println("Marked Gmail message as read: " + messageId);
    }

    private String extractHeader (JsonNode messageRoot, String headerName) {
        JsonNode headers = messageRoot
                .path("payload")
                .path("headers");

        if (!headers.isArray()) {
            return "";
        }

        for (JsonNode header : headers) {
            String name = header.path("name").asText("");

            if (headerName.equalsIgnoreCase(name)) {
                return header.path("value").asText("");
            }
        }

        return "";
    }

}
