package com.aicode.reviewer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aicode.reviewer.config.ReviewerProperties;
import com.aicode.reviewer.enums.FindingCategory;
import com.aicode.reviewer.enums.FindingSeverity;
import com.aicode.reviewer.enums.RiskLevel;
import com.aicode.reviewer.model.entity.ReviewFinding;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskScoringServiceTest {

    private RiskScoringService service;

    @BeforeEach
    void setup() {
        ReviewerProperties properties = new ReviewerProperties();
        ReviewerProperties.RiskThresholds thresholds = new ReviewerProperties.RiskThresholds();
        thresholds.setLow(30);
        thresholds.setMedium(60);
        properties.setRiskThresholds(thresholds);
        service = new RiskScoringService(properties);
    }

    @Test
    void calculatesRiskFromFindingsAndFootprint() {
        ReviewFinding critical = new ReviewFinding();
        critical.setSeverity(FindingSeverity.CRITICAL);
        critical.setCategory(FindingCategory.BUG);
        double score = service.calculateScore(List.of(critical), 50, 4, 120, 40);
        assertThat(score).isBetween(60.0, 100.0);
        assertThat(service.classify(score)).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void lowSeverityLeadsToLowRisk() {
        ReviewFinding low = new ReviewFinding();
        low.setSeverity(FindingSeverity.LOW);
        double score = service.calculateScore(List.of(low), 5, 1, 5, 1);
        assertThat(service.classify(score)).isEqualTo(RiskLevel.LOW);
    }
}
