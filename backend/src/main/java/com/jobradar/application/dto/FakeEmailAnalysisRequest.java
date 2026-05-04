package com.jobradar.application.dto;

public record FakeEmailAnalysisRequest(Long applicationId,
                                       String subject,
                                       String sender,
                                       String body) {
}
