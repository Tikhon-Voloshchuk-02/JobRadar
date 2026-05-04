package com.jobradar.application.service;

import com.jobradar.application.dto.AiSuggestionResponse;
import com.jobradar.application.model.Application;
import com.jobradar.application.model.ai.AiSuggestion;
import com.jobradar.application.model.ai.SuggestionStatus;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.AiSuggestionRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class AiSuggestionService {
    private final AiSuggestionRepository aiSuggestionRepository;

    public AiSuggestionService(AiSuggestionRepository aiSuggestionRepository) {
        this.aiSuggestionRepository = aiSuggestionRepository;
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
    // We receive a related request
        Application application = suggestion.getApplication();

        application.setStatus(suggestion.getSuggestedStatus());
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
}
