package com.jobradar.application.controller;

import com.jobradar.application.gmail.GmailConnectionStatusResponse;
import com.jobradar.application.service.GmailService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String, String> connect(Authentication auth) {
        String url = gmailService.getConnectUrl(auth);
        return Map.of("url", url);
    }

    @PostMapping("/disconnect")
    public void disconnect(Authentication auth) {
        gmailService.disconnect(auth);
    }
}
