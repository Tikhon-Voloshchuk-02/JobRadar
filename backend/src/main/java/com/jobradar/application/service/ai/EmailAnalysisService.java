package com.jobradar.application.service.ai;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;

import com.jobradar.application.service.ai.provider.AiProperties;
import com.jobradar.application.service.ai.provider.AiProvider;
import com.jobradar.application.service.ai.provider.AiProviderManager;
import org.springframework.stereotype.Service;

@Service
public class EmailAnalysisService {

    private final AiProperties aiProperties;
    private final AiProviderManager aiProviderManager;

    public EmailAnalysisService(
                                AiProperties aiProperties,
                                AiProviderManager aiProviderManager){
        this.aiProperties=aiProperties;
        this.aiProviderManager=aiProviderManager;
    }

    public EmailAnalysisResult analyze(GmailMessageDto email){
        AiProvider provider = aiProviderManager.getProvider(aiProperties.getProvider());

        return provider.analyze(email);
    }

}
