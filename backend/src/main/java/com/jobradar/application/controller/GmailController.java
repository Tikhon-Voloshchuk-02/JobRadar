package com.jobradar.application.controller;

import com.jobradar.application.dto.gmail.*;
import com.jobradar.application.dto.google.GoogleTokenResponse;
import com.jobradar.application.gmail.GmailConnection;
import com.jobradar.application.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.service.gmail.GmailConnectionService;
import com.jobradar.application.service.gmail.GmailEmailProcessingService;
import com.jobradar.application.service.gmail.GmailService;
import com.jobradar.application.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
public class GmailController {

    @Value("${app.frontend-url}")
    private String frontendUrl;


    private final GmailService gmailService;
    private final GmailConnectionService gmailConnectionService;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;
    private final GmailEmailProcessingService gmailEmailProcessingService;

    public GmailController(GmailService gmailService,
                           GoogleOAuthService googleOAuthService,
                           GmailConnectionService gmailConnectionService,
                           UserRepository userRepository,
                           GmailEmailProcessingService gmailEmailProcessingService) {

        this.gmailService = gmailService;
        this.googleOAuthService = googleOAuthService;
        this.gmailConnectionService = gmailConnectionService;
        this.userRepository = userRepository;
        this.gmailEmailProcessingService=gmailEmailProcessingService;
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
    public void oauthCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) throws IOException {
        try {
            GoogleTokenResponse tokenResponse =
                    googleOAuthService.exchangeCodeForTokens(code);

            gmailConnectionService.saveTokens(state, tokenResponse);

            response.sendRedirect(frontendUrl + "/user?gmail=connected");
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/user?gmail=error");
        }
    }

    @GetMapping("/messages")
    public GmailMessageListResponse listMessages(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return gmailService.listMessages(user);
    }

    @GetMapping("/messages/{messageId}")
    public GmailMessageDetailResponse getMessage(@PathVariable String messageId, Authentication auth){

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return gmailService.getMessage(user, messageId);
    }

    @GetMapping("/emails")
    public List<GmailMessageDto> getEmails(Authentication auth) {
        return gmailEmailProcessingService.fetchRecentEmails(auth);
    }

    @GetMapping("/emails/analyze")
    public List<GmailEmailAnalysisResponse> analyzeEmails(Authentication auth){
        return gmailEmailProcessingService.analyzeRecentEmails(auth);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect(Authentication auth) {
        gmailService.disconnect(auth);

        return ResponseEntity.ok(Map.of(
                "message", "Gmail disconnected successfully"
        ));
    }

    @PostMapping("/mock-connect")
    public GmailConnectionStatusResponse mockConnect(Authentication auth) {
        return gmailService.mockConnect(auth);
    }

    @PatchMapping("/auto-update")
    public ResponseEntity<?> setAutoUpdate(Authentication auth, @RequestBody AutoUpdateRequest request){

        GmailConnection connection = gmailService.setAutoUpdate(auth, request.isEnabled());
        return ResponseEntity.ok(
                Map.of("autoUpdateEnabled", connection.isAutoUpdateEnabled())
        );
    }
}
