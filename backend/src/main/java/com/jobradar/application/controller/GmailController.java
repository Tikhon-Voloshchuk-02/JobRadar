package com.jobradar.application.controller;

import com.jobradar.application.dto.GoogleTokenResponse;
import com.jobradar.application.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.service.GmailService;
import com.jobradar.application.service.GoogleOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
public class GmailController {

    private final GmailService gmailService;
    private final GoogleOAuthService googleOAuthService;

    public GmailController(GmailService gmailService,
                           GoogleOAuthService googleOAuthService) {
        this.gmailService = gmailService;
        this.googleOAuthService=googleOAuthService;
    }

    @GetMapping("/status")
    public GmailConnectionStatusResponse getStatus(Authentication auth) {
        return gmailService.getStatus(auth);
    }

    @GetMapping("/connect")
    public Map<String, String> connect(Authentication authentication) {
        return Map.of(
                "url",
                gmailService.buildGoogleOAuthUrl(authentication)
        );
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<Map<String, String>> oauthCallback(
            @RequestParam String code,
            @RequestParam String state
    ) {

        GoogleTokenResponse tokenResponse =
                googleOAuthService.exchangeCodeForTokens(code);

        return ResponseEntity.ok(Map.of(
                "message", "Token exchange successful",
                "state", state
        ));
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
