package com.jobradar.application.gmail;

import java.time.LocalDateTime;

public record GmailConnectionStatusResponse(
        boolean connected,
        String googleEmail,
        LocalDateTime connectedAt
) {
}
