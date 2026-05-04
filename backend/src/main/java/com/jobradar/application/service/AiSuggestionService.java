package com.jobradar.application.service;

import com.jobradar.application.dto.AiSuggestionResponse;
import com.jobradar.application.dto.FakeEmailAnalysisRequest;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.StatusChangeSource;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.model.ai.AiSuggestion;
import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.model.ai.SuggestionStatus;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.AiSuggestionRepository;

import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class AiSuggestionService {
    private final AiSuggestionRepository aiSuggestionRepository;

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public AiSuggestionService(AiSuggestionRepository aiSuggestionRepository,
                               ApplicationRepository applicationRepository,
                               StatusHistoryRepository statusHistoryRepository) {
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository=statusHistoryRepository;

    }

    public List<AiSuggestion> getPendingSuggestions(User user){
        return aiSuggestionRepository.findByApplication_UserAndSuggestionStatus(user, SuggestionStatus.PENDING);
    }

    /**
     * Rejects the AI suggestion.
     *
     * Logic:
     * 1. Checks if the suggestion exists
     * 2. Checks if the suggestion belongs to the current user
     * 3. Checks that the suggestion has not yet been processed (PENDING)
     * 4. Sets the REJECTED status
     * 5. Saves the changes and returns the DTO
     *
     * Important:
     * - The status of the application does NOT change
     * - The status history is NOT affected
     *
     * @param suggestionId Suggestion - ID
     * @param user current user
     * @return AiSuggestionResponse with updated status
     */
    @Transactional
    public AiSuggestionResponse rejectSuggestion(Long suggestionId, User user){
        AiSuggestion suggestion = aiSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("suggestion not found"));

        if(!suggestion.getApplication().getUser().getId().equals(user.getId())){
            throw new RuntimeException("Access Denied");
        }

        // Check: suggestion has not been processed yet
        if(suggestion.getSuggestionStatus() != SuggestionStatus.PENDING){
            throw new RuntimeException("SUggestion is already processed");
        }

        //Changing the suggestion status to - REJECTED
        suggestion.setSuggestionStatus(SuggestionStatus.REJECTED);

        AiSuggestion saved = aiSuggestionRepository.save(suggestion);

        return toResponse(saved);
    }

    /**
     * Accepts the AI suggestion and updates the application status.
     *
     * Logic:
     * - Checks the existence of the suggestion
     * - Checks whether the suggestion belongs to the user
     * - Checks that the suggestion is in the PENDING state
     * - Updates the status of the request to the suggestedStatus
     * - Marks the suggestion as ACCEPTED
     * - Saves the changes and returns the DTO
     * Important:
     * - The status of the request is changing
     * - Status history IS NOT updated YET (to be added later)
     *
     * @param suggestionId -  ID
     * @param user current user
     * @return AiSuggestionResponse with updated status
     */
    @Transactional
    public AiSuggestionResponse acceptSuggestion(Long suggestionId, User user){

        AiSuggestion suggestion = aiSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("Suggestion not found"));

    // Verification: suggestion belongs to the user
        if (!suggestion.getApplication().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
    // Verification: suggestion has not been processed yet
        if (suggestion.getSuggestionStatus() != SuggestionStatus.PENDING) {
            throw new RuntimeException("Suggestion is already processed");
        }
    // Get the related application
        Application application = suggestion.getApplication();

        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = suggestion.getSuggestedStatus();

        if (oldStatus != newStatus) {
            application.setStatus(newStatus);

            Application savedApplication = applicationRepository.save(application);

            StatusHistory historyEntry = new StatusHistory(
                    oldStatus,
                    newStatus,
                    savedApplication,
                    StatusChangeSource.AI_GMAIL
            );

            statusHistoryRepository.save(historyEntry);
        }

        suggestion.setSuggestionStatus(SuggestionStatus.ACCEPTED);

        AiSuggestion saved = aiSuggestionRepository.save(suggestion);
        return toResponse(saved);

    }

    public AiSuggestionResponse toResponse(AiSuggestion suggestion) {
        return new AiSuggestionResponse(
                suggestion.getId(),
                suggestion.getApplication().getId(),
                suggestion.getApplication().getCompany(),
                suggestion.getApplication().getPosition(),
                suggestion.getCurrentStatus(),
                suggestion.getSuggestedStatus(),
                suggestion.getConfidence(),
                suggestion.getReason(),
                suggestion.getEmailSnippet(),
                suggestion.getSource(),
                suggestion.getSuggestionStatus(),
                suggestion.getCreatedAt()
        );
    }

    public List<AiSuggestionResponse> getPendingSuggestionResponses(User user) {
        return getPendingSuggestions(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Analyzes a "quasi-email" and creates an AiSuggestion.
     *
     * This is a temporary logic (Fake AI) that:
     * - accepts the text of the letter (subject + sender + body)
     * - searches for keywords
     * - determines the expected status of the application
     * - creates an AiSuggestion with the PENDING status

     * Logic:
     * 1. receive the Application by ID
     * 2. verify that the application belongs to the current user
     * 3. collect the text of the letter
     * 4. apply simple rules (keywords)
     * 5. create AiSuggestion
     * 6. save and return DTO
     *
     * @param request data "letters"
     * @param user current user
     * @return AiSuggestionResponse
     */
    @Transactional
    public AiSuggestionResponse analyzeFakeEmail(FakeEmailAnalysisRequest request, User user){
        Application application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

// collect the text of the letter in one line
        String text = (
                request.subject() + " " +
                        request.sender() + " " +
                        request.body()
        ).toLowerCase();

// Default values (if nothing is found)
        ApplicationStatus suggestedStatus = ApplicationStatus.WAITING;
        ConfidenceLevel confidence = ConfidenceLevel.LOW;
        String reason = "Fake AI could not clearly determine the application status.";

// The simplest "AI logic" (keywords)

        // REJECTED
        if (text.contains("unfortunately") ||
                text.contains("regret") ||
                text.contains("not selected")) {

            suggestedStatus = ApplicationStatus.REJECTED;
            confidence = ConfidenceLevel.HIGH;
            reason = "Fake AI detected rejection-related keywords.";
        }

        // INTERVIEW
        else if (text.contains("interview") ||
                text.contains("call") ||
                text.contains("meeting")) {

            suggestedStatus = ApplicationStatus.INTERVIEW;
            confidence = ConfidenceLevel.HIGH;
            reason = "Fake AI detected interview-related keywords.";
        }

        // OFFER
        else if (text.contains("offer") ||
                text.contains("congratulations")) {

            suggestedStatus = ApplicationStatus.OFFER;
            confidence = ConfidenceLevel.HIGH;
            reason = "Fake AI detected offer-related keywords.";
        }

        // WAITING / CONFIRMATION
        else if (text.contains("received your application") ||
                text.contains("we will review")) {

            suggestedStatus = ApplicationStatus.WAITING;
            confidence = ConfidenceLevel.MEDIUM;
            reason = "Fake AI detected application confirmation / waiting-related keywords.";
        }

//Create AI-Suggestions
        AiSuggestion suggestion = new AiSuggestion();
        suggestion.setApplication(application);
        suggestion.setCurrentStatus(application.getStatus());
        suggestion.setSuggestedStatus(suggestedStatus);
        suggestion.setConfidence(confidence);
        suggestion.setReason(reason);

// saving Part of the email (then we will show it to the user)
        suggestion.setEmailSnippet(
                request.body().length() > 500
                        ? request.body().substring(0, 500)
                        : request.body()
        );

//Save in DB
        AiSuggestion saved = aiSuggestionRepository.save(suggestion);
        return toResponse(saved);
    }
}
