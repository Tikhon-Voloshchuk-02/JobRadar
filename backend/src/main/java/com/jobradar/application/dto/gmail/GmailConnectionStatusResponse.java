package com.jobradar.application.dto.gmail;

import java.time.LocalDateTime;

public record GmailConnectionStatusResponse(
        boolean connected,
        String googleEmail,
        LocalDateTime connectedAt
) {
}
