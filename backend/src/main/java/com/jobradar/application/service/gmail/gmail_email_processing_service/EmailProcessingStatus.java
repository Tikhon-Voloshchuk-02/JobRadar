package com.jobradar.application.service.gmail.gmail_email_processing_service;

public enum EmailProcessingStatus {
    IGNORED,
    DETECTED,
    ANALYZED,
    NO_MATCH,
    SUGGESTION_CREATED,
    FAILED
}
