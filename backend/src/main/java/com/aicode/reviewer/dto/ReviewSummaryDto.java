package com.aicode.reviewer.dto;

import com.aicode.reviewer.enums.RiskLevel;
import com.aicode.reviewer.model.entity.PullRequestReview;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewSummaryDto {
    Long id;
    Long githubPrId;
    String repository;
    Integer prNumber;
    String title;
    String author;
    double riskScore;
    RiskLevel riskLevel;
    String summary;
    int findings;
    Instant createdAt;
    Instant updatedAt;

    public static ReviewSummaryDto fromEntity(PullRequestReview review) {
        return ReviewSummaryDto.builder()
                .id(review.getId())
                .githubPrId(review.getGithubPrId())
                .repository(review.getRepoOwner() + "/" + review.getRepoName())
                .prNumber(review.getPrNumber())
                .title(review.getTitle())
                .author(review.getAuthor())
                .riskScore(review.getRiskScore())
                .riskLevel(review.getRiskLevel())
                .summary(review.getSummary())
                .findings(review.getFindings().size())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
