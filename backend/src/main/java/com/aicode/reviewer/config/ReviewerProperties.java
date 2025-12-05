package com.aicode.reviewer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "reviewer")
public class ReviewerProperties {
    private int maxFiles = 150;
    private int maxDiffSize = 20000;
    private boolean notificationsEnabled = true;
    private RiskThresholds riskThresholds = new RiskThresholds();

    @Data
    public static class RiskThresholds {
        private double low = 30;
        private double medium = 60;
    }
}
