package com.jobradar.application.dto;

import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.model.ai.SuggestionStatus;

import java.time.LocalDateTime;

public record AiSuggestionResponse(
        Long id,
        Long applicationId,
        String company,
        String position,
        ApplicationStatus currentStatus,
        ApplicationStatus suggestedStatus,
        ConfidenceLevel confidence,
        String reason,
        String emailSnippet,
        String source,
        SuggestionStatus suggestionStatus,
        LocalDateTime createdAt
) {
}
