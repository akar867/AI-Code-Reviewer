package com.aicode.reviewer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    private String provider = "openai";
    private ProviderConfig openai = new ProviderConfig();
    private ProviderConfig anthropic = new ProviderConfig();

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String model;
        private String endpoint;
    }
}
