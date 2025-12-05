package com.aicode.reviewer.service;

import com.aicode.reviewer.model.entity.PullRequestReview;
import com.aicode.reviewer.service.github.GitHubClient;
import com.aicode.reviewer.service.github.GitHubSignatureVerifier;
import com.aicode.reviewer.service.github.PullRequestContext;
import com.aicode.reviewer.service.notification.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Set<String> SUPPORTED_ACTIONS = Set.of("opened", "synchronize", "reopened", "ready_for_review");

    private final ObjectMapper objectMapper;
    private final GitHubSignatureVerifier signatureVerifier;
    private final GitHubClient gitHubClient;
    private final ReviewService reviewService;
    private final NotificationService notificationService;

    public void handleWebhook(String event, String signature, String payload) throws Exception {
        if (!signatureVerifier.isSignatureValid(payload, signature)) {
            throw new IllegalArgumentException("Invalid GitHub signature");
        }
        if (!"pull_request".equals(event)) {
            log.debug("Ignoring event {}", event);
            return;
        }

        JsonNode root = objectMapper.readTree(payload);
        String action = root.path("action").asText();
        if (!SUPPORTED_ACTIONS.contains(action)) {
            log.debug("Ignoring PR action {}", action);
            return;
        }

        JsonNode pr = root.path("pull_request");
        PullRequestContext context = new PullRequestContext(
                pr.path("id").asLong(),
                root.path("repository").path("owner").path("login").asText(),
                root.path("repository").path("name").asText(),
                pr.path("number").asInt(),
                pr.path("title").asText(),
                pr.path("user").path("login").asText()
        );

        String diffUrl = pr.path("diff_url").asText();
        String diff = gitHubClient.fetchDiff(diffUrl);
        if (!StringUtils.hasText(diff)) {
            log.warn("Empty diff for PR {}.{}", context.repoSlug(), context.prNumber());
            return;
        }

        PullRequestReview review = reviewService.createAutomatedReview(context, diff);
        notificationService.publishReviewComment(review);
    }
}
