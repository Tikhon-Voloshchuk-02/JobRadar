package com.jobradar.application.controller;

import com.jobradar.application.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.service.GmailService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
public class GmailController {

    private final GmailService gmailService;

    public GmailController(GmailService gmailService) { this.gmailService = gmailService; }

    @GetMapping("/status")
    public GmailConnectionStatusResponse getStatus(Authentication auth) {
        return gmailService.getStatus(auth);
    }

    @GetMapping("/connect")
    public Map<String, String> connect() {
        return Map.of(
                "url",
                gmailService.buildGoogleOAuthUrl()
        );
    }

    @GetMapping("/oauth/callback")
    public Map<String, String> oauthCallback(@RequestParam String code) {
        return Map.of(
                "message", "Google OAuth callback received",
                "code", code
        );
    }

    @PostMapping("/disconnect")
    public void disconnect(Authentication auth) {
        gmailService.disconnect(auth);
    }

    @PostMapping("/mock-connect")
    public GmailConnectionStatusResponse mockConnect(Authentication auth) {
        return gmailService.mockConnect(auth);
    }
}
