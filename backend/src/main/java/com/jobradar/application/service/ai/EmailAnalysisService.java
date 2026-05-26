package com.jobradar.application.service.ai;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;

import com.jobradar.application.service.ai.provider.AiProperties;
import com.jobradar.application.service.ai.provider.AiProvider;
import com.jobradar.application.service.ai.provider.AiProviderType;
import org.springframework.stereotype.Service;

@Service
public class EmailAnalysisService {

    private final AiProvider aiProvider;
    private final AiProperties aiProperties;

    public EmailAnalysisService(AiProvider aiProvider, AiProperties aiProperties){
        this.aiProvider=aiProvider;
        this.aiProperties=aiProperties;
    }

    public EmailAnalysisResult analyze(GmailMessageDto email){
        if(aiProperties.getProvider() == AiProviderType.RULE_BASED){
            return aiProvider.analyze(email);
        }

        return aiProvider.analyze(email);
    }

}
