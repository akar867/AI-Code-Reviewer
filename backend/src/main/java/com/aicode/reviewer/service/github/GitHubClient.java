package com.aicode.reviewer.service.github;

import com.aicode.reviewer.config.GithubProperties;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubClient {

    private final WebClient githubWebClient;
    private final GithubProperties properties;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    public String fetchDiff(String diffUrl) {
        if (!StringUtils.hasText(diffUrl)) {
            return "";
        }

        WebClient client = WebClient.builder()
                .defaultHeader("Accept", "application/vnd.github.v3.diff")
                .defaultHeader("Authorization", authHeader())
                .build();

        try {
            return client.get()
                    .uri(diffUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);
        } catch (Exception ex) {
            log.warn("Failed to download diff from {}: {}", diffUrl, ex.getMessage());
            return "";
        }
    }

    public void postReviewComment(String owner, String repo, int prNumber, String body) {
        if (!StringUtils.hasText(body) || !StringUtils.hasText(owner) || !StringUtils.hasText(repo)) {
            return;
        }

        try {
            githubWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/issues/{pr}/comments")
                            .build(owner, repo, prNumber))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Map.of("body", body)))
                    .retrieve()
                    .toBodilessEntity()
                    .block(REQUEST_TIMEOUT);
        } catch (Exception ex) {
            log.warn("Unable to publish comment to PR {}/{}#{}: {}", owner, repo, prNumber, ex.getMessage());
        }
    }

    private String authHeader() {
        return properties.getToken() != null && !properties.getToken().isBlank()
                ? "Bearer " + properties.getToken()
                : "";
    }
}
