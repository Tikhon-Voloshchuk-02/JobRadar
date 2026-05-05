package com.jobradar.application.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        boolean emailVerified,
        boolean gmailConnected,
        LocalDateTime createdAt

) {
}
