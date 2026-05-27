package com.jobradar.application.service.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobradar.application.dto.ai.OpenAiChatRequest;
import com.jobradar.application.dto.ai.OpenAiChatResponse;
import com.jobradar.application.dto.gmail.EmailAnalysisResult;
import com.jobradar.application.dto.gmail.GmailMessageDto;
import com.jobradar.application.model.ApplicationStatus;
import com.jobradar.application.model.ai.ConfidenceLevel;
import com.jobradar.application.service.ai.OpenAiProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class OpenAiProvider implements AiProvider {
    private final OpenAiProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiProvider(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public AiProviderType getType(){
        return AiProviderType.OPENAI;
    }

    @Override
    public EmailAnalysisResult analyze(GmailMessageDto email){
        try{
            OpenAiChatRequest request = new OpenAiChatRequest(
                    properties.getModel(),
                    List.of(
                            new OpenAiChatRequest.Message("system", systemPrompt()),
                            new OpenAiChatRequest.Message("user", userPrompt(email))
                    ),
                    0.1
            );

            OpenAiChatResponse response = webClient
                    .post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiChatResponse.class)
                    .block();


            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return new EmailAnalysisResult(
                        false,
                        null,
                        ConfidenceLevel.LOW,
                        "OpenAI returned empty response"
                );
            }

            String content = response.getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            return parseResult(content);
        } catch (Exception e){
            return new EmailAnalysisResult(
                    false, null,
                    ConfidenceLevel.LOW, "OpenAI analysis failed: " +e.getMessage()
            );
        }

    }

    private EmailAnalysisResult parseResult(String content) throws Exception{
        OpenAiAnalysisJson  json = objectMapper.readValue(content, OpenAiAnalysisJson.class);

        ApplicationStatus status = null;
        if (json.suggestedStatus != null && !json.suggestedStatus.isBlank()) {
            status = ApplicationStatus.valueOf(json.suggestedStatus);
        }

        ConfidenceLevel confidence = ConfidenceLevel.valueOf(json.confidence);

        return new EmailAnalysisResult(
                json.jobRelated,
                status,
                confidence,
                json.reason
        );

    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class OpenAiAnalysisJson {
        public boolean jobRelated;
        public String suggestedStatus;
        public String confidence;
        public String reason;
    }

    private String systemPrompt() {
        return """
                You analyze job application related emails.

                Return ONLY valid JSON.
                No markdown. No explanation outside JSON.

                JSON format:
                {
                  "jobRelated": true,
                  "suggestedStatus": "REJECTED",
                  "confidence": "HIGH",
                  "reason": "Short explanation"
                }

                Rules:
                - jobRelated: true or false
                - suggestedStatus: one of REJECTED, INTERVIEW, OFFER, WAITING, APPLIED or null
                - confidence: LOW, MEDIUM, HIGH
                - reason: short explanation
                """;
    }

    private String userPrompt(GmailMessageDto email) {
        return """
                Analyze this email:

                Subject: %s
                From: %s
                Snippet: %s
                Body: %s
                """.formatted(
                nullToEmpty(email.subject()),
                nullToEmpty(email.from()),
                nullToEmpty(email.snippet()),
                nullToEmpty(email.bodyText())
        );
    }


}
