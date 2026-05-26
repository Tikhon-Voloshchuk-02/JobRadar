package com.jobradar.application.service.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.openai")
public class OpenAiProperties {
    private String apiKey;
    private String model;

    public String getApiKey(){return apiKey;}

    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }

    public void setModel(String model) { this.model = model; }
}
