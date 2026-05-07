package com.jobradar.application.gmail;

import com.jobradar.application.dto.gmail.GmailMessageListResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.service.gmail.GmailTokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GmailApiClient {

    private final GmailTokenService gmailTokenService;
    private final RestClient restClient;

    public GmailApiClient(GmailTokenService gmailTokenService) {
        this.gmailTokenService = gmailTokenService;
        this.restClient = RestClient.create();
    }

    public GmailMessageListResponse listMessages(User user) {
        String accessToken = gmailTokenService.getValidAccessToken(user);

        return restClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=10")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GmailMessageListResponse.class);
    }
}
