package com.jobradar.application.dto.gmail;

import java.time.Instant;

public record GmailMessageDto(
        String gmailMessageId,
        String subject,
        String from,
        String snippet,
        Instant receivedAt
) {
}
