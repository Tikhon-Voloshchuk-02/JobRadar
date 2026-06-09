package com.jobradar.application.service.ai.provider;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;

public interface AiProvider {
    EmailAnalysisResult analyze(GmailMessageDto email);

    AiProviderType getType();
}
