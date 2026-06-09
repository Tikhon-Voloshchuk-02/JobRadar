package com.jobradar.application.service.ai.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    private AiProviderType provider = AiProviderType.RULE_BASED;

    public AiProviderType getProvider() {
        return provider;
    }

    public void setProvider(AiProviderType provider) {
        this.provider = provider;
    }

}
