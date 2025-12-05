package com.aicode.reviewer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "github")
public class GithubProperties {
    private String apiUrl;
    private String token;
    private String webhookSecret;
    private String botUser;
}
