package com.jobradar.application.dto.gmail;

import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.model.ai.EmailEventType;

public record EmailAnalysisResult(
        boolean jobRelated,
        ApplicationStatus suggestedStatus,
        ConfidenceLevel confidence,
        String reason

) {
}
