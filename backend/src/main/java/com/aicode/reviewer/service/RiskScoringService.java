package com.aicode.reviewer.service;

import com.aicode.reviewer.config.ReviewerProperties;
import com.aicode.reviewer.enums.RiskLevel;
import com.aicode.reviewer.model.entity.ReviewFinding;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RiskScoringService {

    private final ReviewerProperties reviewerProperties;

    public double calculateScore(List<ReviewFinding> findings, double llmScore, int changedFiles, int additions, int deletions) {
        double severityScore = findings.stream()
                .mapToDouble(f -> f.getSeverity().toScore())
                .sum();
        double footprint = Math.min(25, Math.log1p(Math.max(0, additions + deletions)) * 4);
        double fileSpread = Math.min(15, Math.log1p(Math.max(1, changedFiles)) * 5);
        double blended = llmScore * 0.4 + severityScore * 0.4 + footprint + fileSpread;
        return Math.min(100, Math.round(blended));
    }

    public RiskLevel classify(double score) {
        ReviewerProperties.RiskThresholds thresholds = reviewerProperties.getRiskThresholds();
        if (score >= thresholds.getMedium()) {
            return RiskLevel.HIGH;
        }
        if (score >= thresholds.getLow()) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
