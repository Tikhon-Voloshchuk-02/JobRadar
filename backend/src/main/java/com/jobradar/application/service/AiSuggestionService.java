package com.jobradar.application.service;

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
        return aiSuggestionRepository.findByApplication_UserSuggestionStatus(user, SuggestionStatus.PENDING);
    }
}
