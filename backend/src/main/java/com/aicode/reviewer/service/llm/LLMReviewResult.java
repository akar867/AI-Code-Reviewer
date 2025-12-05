package com.aicode.reviewer.service.llm;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class LLMReviewResult {
    String summary;
    double riskScore;
    @Singular
    List<LLMReviewFinding> findings;
    @Singular
    List<String> missingTests;

    public static LLMReviewResult empty() {
        return LLMReviewResult.builder()
                .summary("Minimal impact change detected.")
                .riskScore(10)
                .findings(Collections.emptyList())
                .missingTests(Collections.emptyList())
                .build();
    }
}
