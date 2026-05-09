package com.jobradar.application.repository;

import com.jobradar.application.model.ai.AiSuggestion;
import com.jobradar.application.model.ai.SuggestionStatus;
import com.jobradar.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiSuggestionRepository  extends JpaRepository<AiSuggestion, Long> {

    List<AiSuggestion> findByApplication_UserAndSuggestionStatus(User user, SuggestionStatus suggestionStatus);

    long countByApplication_UserAndSuggestionStatus(User user, SuggestionStatus suggestionStatus);
}
