package com.jobradar.application.service.ai.provider;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AiProviderManager {
    private final Map<AiProviderType, AiProvider> providers
            = new EnumMap<>(AiProviderType.class);

    public AiProviderManager(List<AiProvider> aiProviders){
        for(AiProvider provider : aiProviders){
            providers.put(provider.getType(), provider);
        }
    }

    public AiProvider getProvider(AiProviderType type){
        AiProvider provider = providers.get(type);

        if(provider==null){
            throw new IllegalArgumentException("No AI provider found for type: " + type);
        }

        return provider;
    }
}
