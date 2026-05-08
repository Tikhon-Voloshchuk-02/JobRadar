package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailEmailAnalysisResponse;
import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.gmail.GmailConnection;
import com.jobradar.application.gmail.GmailConnectionRepository;

import com.jobradar.application.gmail.ProcessedEmail;
import com.jobradar.application.gmail.ProcessedEmailRepository;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ai.AiSuggestion;
import com.jobradar.application.model.ai.SuggestionStatus;
import com.jobradar.application.repository.AiSuggestionRepository;
import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.service.EmailAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.jobradar.application.model.user.User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class GmailEmailProcessingService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailTokenService gmailTokenService;
    private final UserRepository userRepository;

    private final EmailAnalysisService emailAnalysisService;
    private final ProcessedEmailRepository processedEmailRepository;
    private final RestClient restClient;

    private final AiSuggestionRepository aiSuggestionRepository;
    private final ApplicationRepository applicationRepository;



    public GmailEmailProcessingService(GmailConnectionRepository gmailConnectionRepository,
                                       GmailTokenService gmailTokenService,
                                       EmailAnalysisService emailAnalysisService,
                                       UserRepository userRepository,
                                       ProcessedEmailRepository processedEmailRepository,
                                       AiSuggestionRepository aiSuggestionRepository,
                                       ApplicationRepository applicationRepository) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailTokenService = gmailTokenService;
        this.userRepository = userRepository;
        this.emailAnalysisService=emailAnalysisService;
        this.processedEmailRepository = processedEmailRepository;
        this.restClient = RestClient.create();

        this.aiSuggestionRepository=aiSuggestionRepository;
        this.applicationRepository=applicationRepository;
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

        gmailConnectionRepository
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

            if (processedEmailRepository.existsByUserAndGmailMessageId(user, messageId)) {
                continue;
            }

            Map fullMessage = restClient.get()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            String subject = extractHeader(fullMessage, "Subject");
            String from = extractHeader(fullMessage, "From");
            String snippet = (String) fullMessage.get("snippet");

            String internalDateRaw = (String) fullMessage.get("internalDate");
            String bodyText = extractBodyText(fullMessage);

            Instant receivedAt = Instant.ofEpochMilli(
                    Long.parseLong(internalDateRaw)
            );

            GmailMessageDto dto = new GmailMessageDto(
                    messageId,
                    subject,
                    from,
                    snippet,
                    bodyText,
                    receivedAt
            );

            boolean jobRelated = isJobRelated(dto);

            ProcessedEmail processedEmail = new ProcessedEmail();
            processedEmail.setUser(user);
            processedEmail.setGmailMessageId(messageId);
            processedEmail.setSender(from);
            processedEmail.setSubject(subject);
            processedEmail.setSnippet(snippet);
            processedEmail.setReceivedAt(
                    LocalDateTime.ofInstant(receivedAt, ZoneId.systemDefault())
            );
            processedEmail.setJobRelated(jobRelated);
            processedEmail.setProcessed(true);
            processedEmail.setProcessedAt(LocalDateTime.now());

            processedEmailRepository.save(processedEmail);

            result.add(dto);
        }

        return result;
    }

    /**
     * Extracts readable email body text from a full Gmail API message response.
     *
     * Workflow:
     * 1. Access the root Gmail payload object
     * 2. Traverse nested MIME parts recursively
     * 3. Extract text/plain sections
     * 4. Decode Base64Url encoded content
     *
     * @param fullMessage full Gmail API message response
     * @return extracted plain text body or empty string if unavailable
     */
    private String extractBodyText(Map fullMessage) {
        Map payload = (Map) fullMessage.get("payload");

        if (payload == null) {
            return "";
        }

        return extractBodyFromPayload(payload);
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isJobRelated(GmailMessageDto email) {
        String text = String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet())
        ).toLowerCase();

        return text.contains("job")
                || text.contains("career")
                || text.contains("application")
                || text.contains("applied")
                || text.contains("interview")
                || text.contains("recruit")
                || text.contains("hr")
                || text.contains("offer")
                || text.contains("unfortunately")
                || text.contains("thank you for applying")
                || text.contains("bewerbung")
                || text.contains("karriere")
                || text.contains("stelle")
                || text.contains("position")
                || text.contains("praktikum")
                || text.contains("werkstudent")
                || text.contains("vorstellungsgespräch")
                || text.contains("absage")
                || text.contains("zusage");
    }

    public List<GmailEmailAnalysisResponse> analyzeRecentEmails(Authentication auth) {
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<GmailMessageDto> emails = fetchRecentEmails(auth);

        return emails.stream()
                .map(gmailEmail -> {
                    EmailAnalysisResult analysis = emailAnalysisService.analyze(gmailEmail);

                    if (analysis.jobRelated() && analysis.suggestedStatus() != null) {
                        findMatchingApplication(user, gmailEmail)
                                .ifPresent(application -> createAiSuggestion(application, gmailEmail, analysis));
                    }

                    return new GmailEmailAnalysisResponse(gmailEmail, analysis);
                })
                .toList();
    }

    private Optional<Application> findMatchingApplication(User user, GmailMessageDto email){

        String text = String.join(" ",
                safe(email.subject()),
                safe(email.from()),
                safe(email.snippet())
        ).toLowerCase();

        return applicationRepository.findByUser(user).stream()
                .filter(application ->
                        text.contains(safe(application.getCompany()).toLowerCase())
                        || text.contains(safe(application.getPosition()).toLowerCase())
                )
                .findFirst();
    }

    private void createAiSuggestion( Application application, GmailMessageDto email,  EmailAnalysisResult analysis) {

        AiSuggestion suggestion = new AiSuggestion();

        suggestion.setApplication(application);
        suggestion.setCurrentStatus(application.getStatus());
        suggestion.setSuggestedStatus(analysis.suggestedStatus());
        suggestion.setConfidence(analysis.confidence());
        suggestion.setReason(analysis.reason());

        String preview = email.bodyText() != null && !email.bodyText().isBlank()
                ? email.bodyText()
                : email.snippet();

        if (preview != null) {

            preview = preview
                    .replaceAll("\\s+", " ")
                    .trim();

            if (preview.length() > 500) {
                preview = preview.substring(0, 500) + "...";
            }
        }

        suggestion.setEmailSnippet(preview);

        suggestion.setSuggestionStatus(SuggestionStatus.PENDING);

        aiSuggestionRepository.save(suggestion);
    }

    /**
     * Recursively traverses Gmail MIME payload structure
     * and extracts all text/plain content.
     *
     * Gmail messages may contain:
     * - multipart/alternative
     * - multipart/mixed
     * - nested MIME sections
     * - attachments
     * - HTML + plain text versions
     *
     * This method walks through nested parts and collects
     * readable plain text sections for AI analysis.
     *
     * @param payload Gmail MIME payload or sub-part
     * @return concatenated plain text body content
     */
    private String extractBodyFromPayload(Map payload) {
        String mimeType = (String) payload.get("mimeType");

        if ("text/plain".equalsIgnoreCase(mimeType)) {
            return decodeBody(payload);
        }

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) payload.get("parts");

        if (parts == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (Map<String, Object> part : parts) {
            result.append(extractBodyFromPayload(part)).append(" ");
        }

        return result.toString().trim();
    }

    /**
     * Decodes Gmail message body content.
     *
     * Gmail API returns email body data as Base64Url-encoded text,
     * not regular Base64. This method decodes it into UTF-8 plain text.
     *
     * @param payload Gmail MIME payload containing body.data
     * @return decoded text content or empty string if body data is missing
     */
    private String decodeBody(Map payload) {
        Map body = (Map) payload.get("body");

        if (body == null || body.get("data") == null) {
            return "";
        }

        String data = (String) body.get("data");

        byte[] decodedBytes = Base64.getUrlDecoder().decode(data);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
