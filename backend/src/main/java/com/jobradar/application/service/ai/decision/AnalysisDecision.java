package com.jobradar.application.service.ai.decision;

import com.jobradar.application.dto.gmail.EmailAnalysisResult;

public class AnalysisDecision {
    private final AnalysisDecisionType type;
    private final EmailAnalysisResult result;
    private final String reason;

    public AnalysisDecision(AnalysisDecisionType type,
                            EmailAnalysisResult result,
                            String reason) {
        this.type = type;
        this.result = result;
        this.reason = reason;
    }

    public AnalysisDecisionType getType() { return type; }

    public EmailAnalysisResult getResult() { return result; }

    public String getReason() { return reason; }

    public static AnalysisDecision notJobRelated(String reason){
        return new AnalysisDecision(AnalysisDecisionType.NOT_JOB_RELATED, null, reason);
    }

    public static AnalysisDecision ruleBasedResult(EmailAnalysisResult result, String reason){
        return new AnalysisDecision(AnalysisDecisionType.RULE_BASED_RESULT, result, reason);
    }

    public static AnalysisDecision needsLlm(String reason){
        return new AnalysisDecision(AnalysisDecisionType.NEEDS_LLM, null, reason);
    }
}
