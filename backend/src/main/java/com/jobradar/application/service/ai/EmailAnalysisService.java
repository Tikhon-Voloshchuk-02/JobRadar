package com.jobradar.application.service.ai;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;

import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.service.ai.decision.AnalysisDecision;
import com.jobradar.application.service.ai.decision.AnalysisDecisionType;
import com.jobradar.application.service.ai.provider.*;
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
/*
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

 */


    public EmailAnalysisResult analyze(GmailMessageDto email){
        AiProviderType providerType = aiProperties.getProvider();

        log.info(
                "Email analysis started: provider={}, subject={}, sender={}",
                providerType,  email.subject(),  email.from()
        );

        //HYBRIDE-Pipeline for Email
        if(providerType == AiProviderType.OPENAI){
            return analyzeWithHybridPipeline(email);
        }

        return analyzeWithSingleProvider(email, providerType);
    }

    private EmailAnalysisResult analyzeWithHybridPipeline(GmailMessageDto email){
        AiProvider ruleBasedProvider = aiProviderManager.getProvider(AiProviderType.RULE_BASED);

        RuleBasedAiProvider ruleBased = (RuleBasedAiProvider) ruleBasedProvider;

        AnalysisDecision decision = ruleBased.analyzeDecision(email);

        log.info(
                "Rule-based decision: type={}, reason={}, subject={}, sender={}",
                decision.getType(),  decision.getReason(),  email.subject(),  email.from()
        );

        if (decision.getType() == AnalysisDecisionType.NOT_JOB_RELATED) {
            return new EmailAnalysisResult(
                    false,
                    null,
                    ConfidenceLevel.LOW,
                    decision.getReason(),
                    null,
                    null
            );
        }

        if (decision.getType() == AnalysisDecisionType.RULE_BASED_RESULT) {
            return decision.getResult();
        }

        log.info(
                "Escalating email analysis to OpenAI: subject={}, sender={}, reason={}",
                email.subject(),  email.from(),  decision.getReason()
        );

        AiProvider openAiProvider =  aiProviderManager.getProvider(AiProviderType.OPENAI);
        EmailAnalysisResult openAiResult = openAiProvider.analyze(email);

        log.info(
                "OpenAI email analysis result: jobRelated={}, suggestedStatus={}, confidence={}, reason={}",
                openAiResult.jobRelated(),  openAiResult.suggestedStatus(),  openAiResult.confidence(),  openAiResult.reason()
        );

        if (!openAiResult.jobRelated()
                && openAiResult.reason() != null
                && openAiResult.reason().contains("OpenAI analysis failed")) {

            log.warn(
                    "OpenAI analysis failed after escalation. Returning rule-based fallback as not job-related: subject={}, sender={}",
                    email.subject(),  email.from()
            );

            return new EmailAnalysisResult(
                    false,
                    null,
                    ConfidenceLevel.LOW,
                    "OpenAI analysis failed after rule-based escalation: " + openAiResult.reason(),
                    null,
                    null
            );
        }
        return openAiResult;
    }


    private EmailAnalysisResult analyzeWithSingleProvider(GmailMessageDto email, AiProviderType providerType){
        AiProvider provider = aiProviderManager.getProvider(providerType);
        EmailAnalysisResult result = provider.analyze(email);

        log.info(
                "Email analysis result: provider={}, jobRelated={}, suggestedStatus={}, confidence={}, reason={}",
                providerType,  result.jobRelated(),  result.suggestedStatus(),
                result.confidence(),  result.reason()
        );

        return result;
    }
}
