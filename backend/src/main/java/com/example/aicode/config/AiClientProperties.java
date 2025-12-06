package com.example.aicode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ai")
public class AiClientProperties {

    /**
     * Provider label (openai, anthropic, etc.) purely informational for logs.
     */
    private String provider = "openai";
    /**
     * HTTPS endpoint that accepts chat completion style payloads.
     */
    private String baseUrl = "https://api.openai.com/v1/chat/completions";
    /**
     * Model identifier (e.g., gpt-4o-mini).
     */
    private String model = "gpt-4o-mini";
    /**
     * API key loaded from the environment.
     */
    private String apiKey;
    /**
     * When true, the service returns deterministic mock content.
     */
    private boolean mockMode = true;
    /**
     * HTTP timeout in seconds for outbound requests.
     */
    private int requestTimeoutSeconds = 30;
    /**
     * Optional custom system message appended before prompts.
     */
    private String systemPrompt = "You are an expert software reviewer.";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
