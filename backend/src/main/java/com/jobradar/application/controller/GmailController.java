package com.jobradar.application.controller;

import com.jobradar.application.dto.GmailMessageListResponse;
import com.jobradar.application.dto.google.GoogleTokenResponse;
import com.jobradar.application.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.service.gmail.GmailConnectionService;
import com.jobradar.application.service.gmail.GmailService;
import com.jobradar.application.service.GoogleOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
public class GmailController {

    private final GmailService gmailService;
    private final GmailConnectionService gmailConnectionService;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;

    public GmailController(GmailService gmailService,
                           GoogleOAuthService googleOAuthService,
                           GmailConnectionService gmailConnectionService,
                           UserRepository userRepository) {

        this.gmailService = gmailService;
        this.googleOAuthService = googleOAuthService;
        this.gmailConnectionService = gmailConnectionService;
        this.userRepository = userRepository;
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

        gmailConnectionService.saveTokens(state, tokenResponse);

        return ResponseEntity.ok(Map.of(
                "message", "Gmail connected successfully"
        ));
    }

    @GetMapping("/messages")
    public GmailMessageListResponse listMessages(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return gmailService.listMessages(user);
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
