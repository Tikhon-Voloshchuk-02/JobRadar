package com.jobradar.application.service.gmail.gmailservice;

import com.jobradar.application.model.gmail.GmailOAuthState;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.repository.gmail.GmailOAuthStateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GmailOAuthUrlService {
    private final GmailOAuthStateRepository gmailOAuthStateRepository;
    private final UserRepository userRepository;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${GOOGLE_REDIRECT_URI}")
    private String googleRedirectUri;

    public GmailOAuthUrlService(GmailOAuthStateRepository gmailOAuthStateRepository,
                                UserRepository userRepository) {
        this.gmailOAuthStateRepository = gmailOAuthStateRepository;
        this.userRepository = userRepository;
    }

    public String buildGoogleOAuthUrl(Authentication authentication) {
        String state = createOAuthStateForCurrentUser(authentication);

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope="
                + "openid%20email%20profile%20https://www.googleapis.com/auth/gmail.modify"
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + state;
    }

    private String createOAuthStateForCurrentUser(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        GmailOAuthState oauthState = new GmailOAuthState();

        oauthState.setState(UUID.randomUUID().toString());
        oauthState.setUserId(currentUser.getId());
        oauthState.setCreatedAt(LocalDateTime.now());
        oauthState.setUsed(false);

        gmailOAuthStateRepository.save(oauthState);

        return oauthState.getState();
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
