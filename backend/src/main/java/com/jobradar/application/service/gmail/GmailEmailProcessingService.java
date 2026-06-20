package com.jobradar.application.service.gmail;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailEmailAnalysisResponse;
import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.gmail.GmailConnection;
import com.jobradar.application.repository.gmail.GmailConnectionRepository;

import com.jobradar.application.model.gmail.ProcessedEmail;
import com.jobradar.application.repository.gmail.ProcessedEmailRepository;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ai.AiSuggestion;
import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.model.ai.SuggestionStatus;
import com.jobradar.application.repository.AiSuggestionRepository;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.service.ai.AiSuggestionService;
import com.jobradar.application.service.ai.EmailAnalysisService;
import com.jobradar.application.service.ai.StatusTransitionValidator;
import com.jobradar.application.service.gmail.gmail_email_processing_service.ApplicationMatcher;
import com.jobradar.application.service.gmail.gmail_email_processing_service.GmailMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.jobradar.application.model.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class GmailEmailProcessingService {

    private static final Logger log = LoggerFactory.getLogger(GmailEmailProcessingService.class);

    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailTokenService gmailTokenService;
    private final UserRepository userRepository;

    private final EmailAnalysisService emailAnalysisService;
    private final ProcessedEmailRepository processedEmailRepository;
    private final RestClient restClient;

    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiSuggestionService aiSuggestionService;

    private final GmailMessageParser gmailMessageParser;
    private final JobEmailDetector jobEmailDetector;
    private final ApplicationMatcher applicationMatcher;

    private final StatusTransitionValidator statusTransitionValidator;



    public GmailEmailProcessingService(GmailConnectionRepository gmailConnectionRepository,
                                       GmailTokenService gmailTokenService,
                                       EmailAnalysisService emailAnalysisService,

                                       UserRepository userRepository,
                                       ProcessedEmailRepository processedEmailRepository,

                                       AiSuggestionRepository aiSuggestionRepository,
                                       AiSuggestionService aiSuggestionService,

                                       GmailMessageParser gmailMessageParser,
                                       JobEmailDetector jobEmailDetector,
                                       ApplicationMatcher applicationMatcher,
                                       StatusTransitionValidator statusTransitionValidator) {

        this.gmailConnectionRepository = gmailConnectionRepository;
        this.gmailTokenService = gmailTokenService;
        this.userRepository = userRepository;
        this.emailAnalysisService=emailAnalysisService;
        this.processedEmailRepository = processedEmailRepository;
        this.restClient = RestClient.create();

        this.aiSuggestionService=aiSuggestionService;
        this.aiSuggestionRepository=aiSuggestionRepository;

        this.gmailMessageParser = gmailMessageParser;
        this.jobEmailDetector=jobEmailDetector;
        this.applicationMatcher=applicationMatcher;

        this.statusTransitionValidator=statusTransitionValidator;
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

        //String url = "https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=30&q=Senacor";
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

            String subject = gmailMessageParser.extractHeader(fullMessage, "Subject");
            String from = gmailMessageParser.extractHeader(fullMessage, "From");
            String snippet = (String) fullMessage.get("snippet");
            String bodyText = gmailMessageParser.extractBodyText(fullMessage);

            String internalDateRaw = (String) fullMessage.get("internalDate");


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

            boolean jobRelated = jobEmailDetector.isJobRelated(dto);

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

    public List<GmailEmailAnalysisResponse> analyzeRecentEmails(Authentication auth) {
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GmailConnection connection = gmailConnectionRepository
                .findByUserIdAndConnectedTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("Gmail is not connected"));

        List<GmailMessageDto> emails = fetchRecentEmails(auth);

        return emails.stream()
                .map(gmailEmail -> {
                    EmailAnalysisResult analysis = emailAnalysisService.analyze(gmailEmail);

                    if (analysis.jobRelated() && analysis.suggestedStatus() != null) {
                        applicationMatcher.findMatchingApplication(user, gmailEmail, analysis)
                                .ifPresent(application -> createAiSuggestion(connection, application, gmailEmail, analysis));
                    }

                    return new GmailEmailAnalysisResponse(gmailEmail, analysis);
                })
                .toList();
    }

    private boolean createAiSuggestion( GmailConnection gmailConnection,
                                     Application application,
                                     GmailMessageDto email,
                                     EmailAnalysisResult analysis) {

        log.info(
                "AI status transition check: applicationId={}, company={}, currentStatus={}, suggestedStatus={}, confidence={}, subject={}",
                application.getId(),
                application.getCompany(),
                application.getStatus(),
                analysis.suggestedStatus(),
                analysis.confidence(),
                email.subject()
        );

        //StatusTransitionValidator
        if(!statusTransitionValidator.isValid(
                application.getStatus(),
                analysis.suggestedStatus()
        )){
            log.warn(
                    "Invalid AI status transition ignored: applicationId={}, currentStatus={}, suggestedStatus={}, emailSubject={}",
                    application.getId(),
                    application.getStatus(),
                    analysis.suggestedStatus(),
                    email.subject()
            );
            return false;
        }

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
        suggestion.setGmailMessageId(email.gmailMessageId());

        suggestion.setSuggestionStatus(SuggestionStatus.PENDING);

        AiSuggestion saved = aiSuggestionRepository.save(suggestion);
        log.info(
                "AI suggestion saved: suggestionId={}, applicationId={}, company={}, currentStatus={}, suggestedStatus={}, confidence={}",
                saved.getId(),
                application.getId(),
                application.getCompany(),
                saved.getCurrentStatus(),
                saved.getSuggestedStatus(),
                saved.getConfidence()
        );

        if (gmailConnection.isAutoUpdateEnabled()
                && saved.getConfidence() == ConfidenceLevel.HIGH
                && saved.getSuggestedStatus() != null) {

            log.info(
                    "Auto-update enabled: accepting AI suggestion automatically, suggestionId={}, applicationId={}, suggestedStatus={}",
                    saved.getId(),
                    application.getId(),
                    saved.getSuggestedStatus()
            );

            aiSuggestionService.acceptSuggestionInternal(
                    saved,
                    application.getUser()
            );
        }
        return true;
    }

    public GmailProcessingResult  processSingleEmail(User user, String messageId, String accessToken) {

        if (processedEmailRepository.existsByUserAndGmailMessageId(user, messageId)) {
            log.debug("Email already processed: {}", messageId);
            return new GmailProcessingResult(false, false);
        }

        Map fullMessage = restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        String subject = gmailMessageParser.extractHeader(fullMessage, "Subject");
        String from = gmailMessageParser.extractHeader(fullMessage, "From");
        String bodyText = gmailMessageParser.extractBodyText(fullMessage);
        String snippet = (String) fullMessage.get("snippet");


        String internalDateRaw = (String) fullMessage.get("internalDate");
        Instant receivedAt = Instant.ofEpochMilli(Long.parseLong(internalDateRaw));

        GmailMessageDto dto = new GmailMessageDto(
                messageId,
                subject,
                from,
                snippet,
                bodyText,
                receivedAt
        );
        log.info(
                "Gmail email fetched: messageId={}, subject={}, sender={}",
                messageId,
                subject,
                from
        );

        boolean jobRelated = jobEmailDetector.isJobRelated(dto);

        log.info(
                "Job email detector result: messageId={}, jobRelated={}, subject={}, sender={}",
                messageId,
                jobRelated,
                subject,
                from
        );

        if (!jobRelated) {
            ProcessedEmail processedEmail = new ProcessedEmail();
            processedEmail.setUser(user);
            processedEmail.setGmailMessageId(messageId);
            processedEmail.setSender(from);
            processedEmail.setSubject(subject);
            processedEmail.setSnippet(snippet);
            processedEmail.setReceivedAt(
                    LocalDateTime.ofInstant(receivedAt, ZoneId.systemDefault())
            );
            processedEmail.setJobRelated(false);
            processedEmail.setProcessed(true);
            processedEmail.setProcessedAt(LocalDateTime.now());

            processedEmailRepository.save(processedEmail);

            log.info(
                    "Email ignored as not job-related: messageId={}, subject={}, sender={}",
                    messageId,
                    subject,
                    from
            );
            return new GmailProcessingResult(false, false);
        }

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


        GmailConnection connection = gmailConnectionRepository
                .findByUserIdAndConnectedTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("Gmail is not connected"));

        EmailAnalysisResult analysis = emailAnalysisService.analyze(dto);

        log.info(
                "Email analysis completed: messageId={}, jobRelated={}, suggestedStatus={}, confidence={}, reason={}",
                messageId,
                analysis.jobRelated(),
                analysis.suggestedStatus(),
                analysis.confidence(),
                analysis.reason()
        );

        if (analysis.jobRelated() && analysis.suggestedStatus() != null) {
            Optional<Application> matchingApplication = applicationMatcher.findMatchingApplication(user, dto, analysis);

            if (matchingApplication.isPresent()) {
                boolean suggestionCreated =
                        createAiSuggestion(connection, matchingApplication.get(), dto, analysis);

                log.info(
                        "AI suggestion creation requested: messageId={}, matchedApplicationId={}, company={}, suggestedStatus={}",
                        messageId,
                        matchingApplication.get().getId(),
                        matchingApplication.get().getCompany(),
                        analysis.suggestedStatus()
                );

                return new GmailProcessingResult(true, suggestionCreated);

            } else {
                log.info(
                        "No matching application found after analysis: messageId={}, subject={}, sender={}, suggestedStatus={}",
                        messageId,
                        subject,
                        from,
                        analysis.suggestedStatus()
                );

                return new GmailProcessingResult(true, false);
            }
        } else {
            log.info(
                    "No suggestion created from email analysis: messageId={}, jobRelated={}, suggestedStatus={}, reason={}",
                    messageId,
                    analysis.jobRelated(),
                    analysis.suggestedStatus(),
                    analysis.reason()
            );

            return new GmailProcessingResult(analysis.jobRelated(), false);
        }
    }

    public record GmailProcessingResult(
            boolean jobRelated,
            boolean suggestionCreated
    ) {
    }

}
