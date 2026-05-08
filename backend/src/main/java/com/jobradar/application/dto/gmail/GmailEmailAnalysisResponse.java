package com.jobradar.application.dto.gmail;

public record GmailEmailAnalysisResponse(
        GmailMessageDto email,
        EmailAnalysisResult analysis
) {
}
