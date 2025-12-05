package com.aicode.reviewer.service.notification;

import com.aicode.reviewer.config.ReviewerProperties;
import com.aicode.reviewer.model.entity.PullRequestReview;
import com.aicode.reviewer.model.entity.ReviewFinding;
import com.aicode.reviewer.service.github.GitHubClient;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ReviewerProperties reviewerProperties;
    private final GitHubClient gitHubClient;

    public void publishReviewComment(PullRequestReview review) {
        if (!reviewerProperties.isNotificationsEnabled()) {
            return;
        }
        if (review.getRepoOwner() == null || review.getRepoName() == null || review.getPrNumber() == null) {
            return;
        }

        String summary = formatSummary(review);
        gitHubClient.postReviewComment(review.getRepoOwner(), review.getRepoName(), review.getPrNumber(), summary);
    }

    private String formatSummary(PullRequestReview review) {
        String findings = review.getFindings().stream()
                .sorted(Comparator.comparing(f -> f.getSeverity().toScore(), Comparator.reverseOrder()))
                .limit(5)
                .map(this::formatFinding)
                .collect(Collectors.joining("\n"));
        return "🤖 Automated Review Summary\n"
                + "Risk score: " + review.getRiskScore() + " (" + review.getRiskLevel() + ")\n"
                + "Summary: " + review.getSummary() + "\n\n"
                + (findings.isBlank() ? "No blocking issues detected." : findings)
                + "\n\n➡️ View full report in the dashboard.";
    }

    private String formatFinding(ReviewFinding finding) {
        return "- [" + finding.getSeverity() + "] " + finding.getFilePath() + ": " + finding.getMessage();
    }
}
