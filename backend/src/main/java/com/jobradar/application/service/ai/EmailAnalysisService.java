package com.jobradar.application.service.ai;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;

import com.jobradar.application.service.ai.provider.AiProperties;
import com.jobradar.application.service.ai.provider.AiProvider;
import com.jobradar.application.service.ai.provider.AiProviderManager;
import com.jobradar.application.service.ai.provider.AiProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(EmailAnalysisService.class);

    private final AiProperties aiProperties;
    private final AiProviderManager aiProviderManager;

    public EmailAnalysisService(
                                AiProperties aiProperties,
                                AiProviderManager aiProviderManager){
        this.aiProperties=aiProperties;
        this.aiProviderManager=aiProviderManager;
    }

    public EmailAnalysisResult analyze(GmailMessageDto email){

        AiProviderType providerType = aiProperties.getProvider();

        log.info(
                "Email analysis started: provider={}, subject={}, sender={}",
                providerType,
                email.subject(),
                email.from()
        );

        AiProvider provider = aiProviderManager.getProvider(providerType);

        EmailAnalysisResult result = provider.analyze(email);

        log.info(
                "Email analysis result: provider={}, jobRelated={}, suggestedStatus={}, confidence={}, reason={}",
                providerType,
                result.jobRelated(),
                result.suggestedStatus(),
                result.confidence(),
                result.reason()
        );

        if(providerType == AiProviderType.OPENAI
                && !result.jobRelated()
                && result.reason() != null
                && result.reason().contains("OpenAI analysis failed")) {

            log.warn(
                    "OpenAI analysis failed, falling back to RULE_BASED: subject={}, sender={}, reason={}",
                    email.subject(),
                    email.from(),
                    result.reason()
            );

            AiProvider fallbackProvider =
                    aiProviderManager.getProvider(AiProviderType.RULE_BASED);

            EmailAnalysisResult fallbackResult = fallbackProvider.analyze(email);
            log.info(
                    "Fallback email analysis result: provider={}, jobRelated={}, suggestedStatus={}, confidence={}, reason={}",
                    AiProviderType.RULE_BASED,
                    fallbackResult.jobRelated(),
                    fallbackResult.suggestedStatus(),
                    fallbackResult.confidence(),
                    fallbackResult.reason()
            );

            return fallbackResult;

        }

        return result;


    }

}
