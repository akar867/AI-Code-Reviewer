package com.example.aicode.service.ai;

import com.example.aicode.config.AiClientProperties;
import com.example.aicode.dto.ReviewResult;
import com.example.aicode.exception.AiReviewException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);

    public static final String PROMPT_TEMPLATE = "Review the following %s code. Identify concrete issues, offer actionable " +
            "suggestions, rate the overall quality from 1-10, and list any best practices evidenced or missing." +
            "\nRespond strictly with JSON matching this schema: {\\"issues\\": string[], \\" +
            "suggestions\\": string[], \\" +
            "qualityScore\\": number, \\" +
            "bestPractices\\": string[] }.";

    private final RestTemplate restTemplate;
    private final AiClientProperties properties;
    private final ObjectMapper objectMapper;

    public AiReviewService(RestTemplate restTemplate,
                           AiClientProperties properties,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public ReviewResult reviewCode(String code, String language) {
        if (properties.isMockMode() || !StringUtils.hasText(properties.getApiKey())) {
            log.debug("Returning mock AI review (mockMode={}, apiKeyPresent={})", properties.isMockMode(),
                    StringUtils.hasText(properties.getApiKey()));
            return buildMockResult(code, language);
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getBaseUrl(),
                    buildHttpRequest(code, language),
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AiReviewException("AI provider returned status " + response.getStatusCode());
            }
            return parseAiResponse(response.getBody());
        } catch (Exception ex) {
            throw new AiReviewException("Unable to obtain AI review", ex);
        }
    }

    private HttpEntity<Map<String, Object>> buildHttpRequest(String code, String language) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", properties.getSystemPrompt()),
                        Map.of("role", "user", "content", buildUserPrompt(code, language))
                ),
                "response_format", Map.of("type", "json_object")
        );
        return new HttpEntity<>(body, headers);
    }

    private String buildUserPrompt(String code, String language) {
        return PROMPT_TEMPLATE.formatted(language) + "\n\n" + code;
    }

    private ReviewResult parseAiResponse(String payload) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        String jsonPayload = contentNode.isMissingNode() || contentNode.isNull()
                ? payload
                : sanitize(contentNode.asText());
        ReviewResult result = objectMapper.readValue(jsonPayload, ReviewResult.class);
        result.setRawResponse(jsonPayload);
        if (result.getQualityScore() == null) {
            result.setQualityScore(5);
        }
        return result;
    }

    private String sanitize(String content) {
        String sanitized = content == null ? "" : content.trim();
        if (sanitized.startsWith("```")) {
            int firstNewline = sanitized.indexOf('\n');
            if (firstNewline > -1) {
                sanitized = sanitized.substring(firstNewline + 1);
            }
            if (sanitized.endsWith("```")) {
                sanitized = sanitized.substring(0, sanitized.length() - 3);
            }
        }
        return sanitized.trim();
    }

    private ReviewResult buildMockResult(String code, String language) {
        ReviewResult result = new ReviewResult();
        result.setIssues(List.of(
                "No input validation present; consider guarding against malformed data.",
                "Error handling is missing which makes debugging production failures harder."
        ));
        result.setSuggestions(List.of(
                "Split large functions into smaller, single-purpose helpers.",
                "Add automated tests that cover success and failure paths.",
                "Document expected inputs/outputs so future reviewers have context."
        ));
        int heuristicScore = Math.max(3, Math.min(9, (int) Math.ceil(Math.log(code.length() + 1))));
        result.setQualityScore(heuristicScore);
        result.setBestPractices(List.of(
                "Keep functions pure where possible",
                "Prefer dependency injection over singletons",
                "Log structured events instead of concatenated strings"
        ));
        result.setRawResponse("Mock response for " + language + " snippet");
        return result;
    }
}
