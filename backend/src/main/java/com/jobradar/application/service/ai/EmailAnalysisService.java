package com.jobradar.application.service.ai;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;

import com.jobradar.application.service.ai.provider.AiProvider;
import org.springframework.stereotype.Service;

@Service
public class EmailAnalysisService {

    private final AiProvider aiProvider;

    public EmailAnalysisService(AiProvider aiProvider){
        this.aiProvider=aiProvider;
    }

    public EmailAnalysisResult analyze(GmailMessageDto email){
        return aiProvider.analyze(email);
    }

}
