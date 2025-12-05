package com.aicode.reviewer.service.llm;

import com.aicode.reviewer.config.LlmProperties;
import com.aicode.reviewer.diff.DiffFile;
import com.aicode.reviewer.diff.DiffHunk;
import com.aicode.reviewer.diff.LineChange;
import com.aicode.reviewer.enums.FindingCategory;
import com.aicode.reviewer.enums.FindingSeverity;
import com.aicode.reviewer.enums.LineChangeType;
import com.aicode.reviewer.service.PromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
public class LlmGateway implements LLMClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final LlmProperties properties;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public LLMReviewResult analyze(String repo, int prNumber, List<DiffFile> files) {
        String prompt = promptBuilder.buildReviewPrompt(repo, prNumber, files);
        String response = callProvider(prompt, false);
        if (!StringUtils.hasText(response)) {
            return heuristicFallback(files);
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            List<LLMReviewFinding> findings = new ArrayList<>();
            if (root.has("findings")) {
                for (JsonNode node : root.get("findings")) {
                    findings.add(LLMReviewFinding.builder()
                            .category(parseCategory(node.path("category").asText()))
                            .severity(parseSeverity(node.path("severity").asText()))
                            .message(node.path("message").asText())
                            .suggestion(node.path("suggestion").asText())
                            .filePath(node.path("file").asText(null))
                            .lineNumber(node.path("line").isNumber() ? node.path("line").asInt() : null)
                            .build());
                }
            }
            List<String> missingTests = new ArrayList<>();
            if (root.has("missingTests")) {
                root.get("missingTests").forEach(node -> missingTests.add(node.asText()));
            }
            double risk = root.path("riskScore").asDouble(estimateRisk(findings));
            String summary = root.path("summary").asText("Automated review summary unavailable.");
            return LLMReviewResult.builder()
                    .summary(summary)
                    .riskScore(risk)
                    .findings(findings)
                    .missingTests(missingTests)
                    .build();
        } catch (Exception ex) {
            log.warn("Unable to parse LLM review payload: {}", ex.getMessage());
            return heuristicFallback(files);
        }
    }

    @Override
    public List<TestRecommendation> generateTests(String repo, List<DiffFile> files) {
        String prompt = promptBuilder.buildTestPrompt(repo, files);
        String response = callProvider(prompt, true);
        if (!StringUtils.hasText(response)) {
            return fallbackTests(files);
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root.isArray()) {
                List<TestRecommendation> tests = new ArrayList<>();
                for (JsonNode node : root) {
                    tests.add(TestRecommendation.builder()
                            .filePath(node.path("file").asText(null))
                            .framework(node.path("framework").asText("JUnit"))
                            .scenario(node.path("scenario").asText())
                            .example(node.path("examplePseudo").asText(node.path("example").asText()))
                            .build());
                }
                return tests;
            }
        } catch (Exception ex) {
            log.warn("Failed to parse test suggestions: {}", ex.getMessage());
        }
        return fallbackTests(files);
    }

    private String callProvider(String prompt, boolean lightweight) {
        LlmProperties.ProviderConfig config = selectProviderConfig();
        if (config == null || !StringUtils.hasText(config.getApiKey())) {
            return null;
        }
        try {
            if (isAnthropic()) {
                return callAnthropic(config, prompt, lightweight);
            }
            return callOpenAi(config, prompt, lightweight);
        } catch (Exception ex) {
            log.warn("LLM provider call failed: {}", ex.getMessage());
            return null;
        }
    }

    private String callOpenAi(LlmProperties.ProviderConfig config, String prompt, boolean lightweight) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", valueOrDefault(config.getModel(), lightweight ? "gpt-4o-mini" : "gpt-4o"));
        request.put("temperature", lightweight ? 0.3 : 0.15);
        request.put("messages", List.of(
                Map.of("role", "system", "content", "You are a meticulous senior engineer."),
                Map.of("role", "user", "content", prompt)
        ));

        return WebClient.builder()
                .baseUrl(defaultIfBlank(config.getEndpoint(), "https://api.openai.com/v1"))
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build()
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.path("choices").path(0).path("message").path("content").asText())
                .block(TIMEOUT);
    }

    private String callAnthropic(LlmProperties.ProviderConfig config, String prompt, boolean lightweight) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", valueOrDefault(config.getModel(), lightweight ? "claude-3-haiku-20240307" : "claude-3-sonnet-20240229"));
        request.put("max_tokens", 1200);
        request.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return WebClient.builder()
                .baseUrl(defaultIfBlank(config.getEndpoint(), "https://api.anthropic.com/v1"))
                .defaultHeader("x-api-key", config.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build()
                .post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.path("content").path(0).path("text").asText())
                .block(TIMEOUT);
    }

    private boolean isAnthropic() {
        return "anthropic".equalsIgnoreCase(properties.getProvider());
    }

    private LlmProperties.ProviderConfig selectProviderConfig() {
        return isAnthropic() ? properties.getAnthropic() : properties.getOpenai();
    }

    private LLMReviewResult heuristicFallback(List<DiffFile> files) {
        List<LLMReviewFinding> findings = new ArrayList<>();
        for (DiffFile file : files) {
            if (file.isBinary()) {
                continue;
            }
            for (DiffHunk hunk : file.getHunks()) {
                for (LineChange change : hunk.getChanges()) {
                    if (change.getType() != LineChangeType.ADDED) {
                        continue;
                    }
                    String content = change.getContent().toLowerCase(Locale.ROOT);
                    if (content.contains("todo") || content.contains("fixme")) {
                        findings.add(LLMReviewFinding.builder()
                                .category(FindingCategory.MAINTAINABILITY)
                                .severity(FindingSeverity.MEDIUM)
                                .message("New TODO/FIXME added; consider resolving before merge.")
                                .suggestion("Resolve the TODO or create a follow-up issue before merging.")
                                .filePath(file.displayName())
                                .lineNumber(change.getNewLineNumber())
                                .build());
                    }
                    if (content.contains("system.out.print") || content.contains("console.log")) {
                        findings.add(LLMReviewFinding.builder()
                                .category(FindingCategory.PERFORMANCE)
                                .severity(FindingSeverity.LOW)
                                .message("Debug logging detected; replace with structured logger.")
                                .suggestion("Use the shared logger or remove noisy statements.")
                                .filePath(file.displayName())
                                .lineNumber(change.getNewLineNumber())
                                .build());
                    }
                    if (content.contains("catch (exception") && !content.contains("throw")) {
                        findings.add(LLMReviewFinding.builder()
                                .category(FindingCategory.BUG)
                                .severity(FindingSeverity.HIGH)
                                .message("Swallowing broad Exception; error will be hidden.")
                                .suggestion("Handle specific exception types or rethrow with context.")
                                .filePath(file.displayName())
                                .lineNumber(change.getNewLineNumber())
                                .build());
                    }
                }
            }
        }
        double risk = estimateRisk(findings);
        List<String> missingTests = inferMissingTests(files);
        return LLMReviewResult.builder()
                .summary("Heuristic review generated without LLM; prioritize missing tests and TODO cleanup.")
                .riskScore(risk)
                .findings(findings)
                .missingTests(missingTests)
                .build();
    }

    private double estimateRisk(List<LLMReviewFinding> findings) {
        if (findings.isEmpty()) {
            return 15;
        }
        double severity = findings.stream().mapToDouble(f -> f.getSeverity().toScore()).average().orElse(20);
        return Math.min(90, 20 + severity + findings.size() * 5);
    }

    private List<String> inferMissingTests(List<DiffFile> files) {
        return files.stream()
                .filter(file -> !file.displayName().toLowerCase(Locale.ROOT).contains("test"))
                .map(file -> "Consider adding regression tests for " + file.displayName())
                .limit(3)
                .collect(Collectors.toList());
    }

    private List<TestRecommendation> fallbackTests(List<DiffFile> files) {
        return files.stream()
                .filter(file -> !file.isBinary())
                .limit(3)
                .map(file -> TestRecommendation.builder()
                        .filePath(file.displayName())
                        .framework(file.displayName().endsWith(".java") ? "JUnit" : "Jest")
                        .scenario("Cover newly added logic in " + file.displayName())
                        .example("Write parameterized tests asserting both success and failure branches.")
                        .build())
                .collect(Collectors.toList());
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String valueOrDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
