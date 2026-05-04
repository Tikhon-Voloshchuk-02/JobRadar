package com.jobradar.application.service;

import com.jobradar.application.dto.AiSuggestionResponse;
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
