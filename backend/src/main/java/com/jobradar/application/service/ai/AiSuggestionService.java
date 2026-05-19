package com.jobradar.application.service.ai;

import com.jobradar.application.dto.ai.AiSuggestionResponse;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.StatusChangeSource;
import com.jobradar.application.model.StatusHistory;
import com.jobradar.application.model.ai.AiSuggestion;
import com.jobradar.application.model.ai.SuggestionStatus;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.AiSuggestionRepository;

import com.jobradar.application.repository.ApplicationRepository;
import com.jobradar.application.repository.StatusHistoryRepository;
import com.jobradar.application.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service

public class AiSuggestionService {
    private final AiSuggestionRepository aiSuggestionRepository;

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    private final UserRepository userRepository;

    public AiSuggestionService(AiSuggestionRepository aiSuggestionRepository,
                               ApplicationRepository applicationRepository,
                               StatusHistoryRepository statusHistoryRepository,
                               UserRepository userRepository) {
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository=statusHistoryRepository;
        this.userRepository=userRepository;

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
        if (suggestion.getSuggestionStatus() != SuggestionStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Suggestion is already processed"
            );
        }

        //Changing the suggestion status to - REJECTED
        suggestion.setSuggestionStatus(SuggestionStatus.REJECTED);

        AiSuggestion saved = aiSuggestionRepository.save(suggestion);

        return toResponse(saved);
    }

    public long countPendingSuggestions(User user) {
        return aiSuggestionRepository.countByApplication_UserAndSuggestionStatus(
                user,
                SuggestionStatus.PENDING
        );
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
    public AiSuggestionResponse acceptSuggestion(Long suggestionId, User user) {

        AiSuggestion suggestion = aiSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("Suggestion not found"));

        AiSuggestion saved = acceptSuggestionInternal(suggestion, user);

        return toResponse(saved);
    }

    @Transactional
    public AiSuggestion acceptSuggestionInternal(AiSuggestion suggestion, User user) {

        if (!suggestion.getApplication().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        if (suggestion.getSuggestionStatus() != SuggestionStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Suggestion is already processed"
            );
        }

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

        return aiSuggestionRepository.save(suggestion);
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

    @Transactional
    public int acceptAllPending(Authentication auth){
        User currentUser = getCurrentUser(auth);

        List<AiSuggestion> pendingSuggestions =
                aiSuggestionRepository.findByApplication_UserAndSuggestionStatus(
                        currentUser,
                        SuggestionStatus.PENDING
                );

        for (AiSuggestion suggestion: pendingSuggestions){
            acceptSuggestion(suggestion.getId(), currentUser);
        }

        return pendingSuggestions.size();
    }

    private User getCurrentUser(Authentication auth){
        String email = auth.getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

}
