package com.example.aicode.service;

import com.example.aicode.dto.ReviewRequest;
import com.example.aicode.dto.ReviewResponse;
import com.example.aicode.dto.ReviewResult;
import com.example.aicode.dto.ReviewSummary;
import com.example.aicode.entity.Review;
import com.example.aicode.exception.ReviewNotFoundException;
import com.example.aicode.repository.ReviewRepository;
import com.example.aicode.service.ai.AiReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final AiReviewService aiReviewService;
    private final ObjectMapper objectMapper;

    public ReviewService(ReviewRepository reviewRepository,
                         AiReviewService aiReviewService,
                         ObjectMapper objectMapper) {
        this.reviewRepository = reviewRepository;
        this.aiReviewService = aiReviewService;
        this.objectMapper = objectMapper;
    }

    public String fetchPatchFromGitHub(String patchUrl) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3.patch");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = rest.exchange(patchUrl, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    public String detectLanguageFromPatch(String patchText) {
        Pattern pattern = Pattern.compile("^diff --git a/(.+?) b/", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(patchText);

        Set<String> languages = new HashSet<>();

        while (matcher.find()) {
            String fileName = matcher.group(1);

            String ext = getFileExtension(fileName);

            switch (ext) {
                case "java": languages.add("Java"); break;
                case "js": languages.add("JavaScript"); break;
                case "ts": languages.add("TypeScript"); break;
                case "py": languages.add("Python"); break;
                case "cpp":
                case "hpp": languages.add("C++"); break;
                case "cs": languages.add("C#"); break;
                case "rb": languages.add("Ruby"); break;
                case "go": languages.add("Go"); break;
                case "sql": languages.add("SQL"); break;
                case "xml": languages.add("XML"); break;
                case "yml":
                case "yaml": languages.add("YAML"); break;
            }
        }

        // If multiple languages detected, choose primary
        if (!languages.isEmpty()) return languages.iterator().next();

        return "Unknown";
    }

    private String getFileExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return index > 0 ? filename.substring(index + 1) : "";
    }

    public ReviewResponse createReview(ReviewRequest request) {
        ReviewResult reviewResult = aiReviewService.reviewCode(request.getCode());
        Review review = new Review();
        review.setlanguage(request.getlanguage());
        review.setSubmittedCode(request.getCode());
        review.setQualityScore(reviewResult.getQualityScore());
        review.setReviewResult(writeResult(reviewResult));
        Review saved = reviewRepository.save(review);
        return new ReviewResponse(saved.getId(), saved.getlanguage(), saved.getSubmittedCode(), saved.getCreatedAt(), reviewResult);
    }

    public List<ReviewSummary> getRecentReviews() {
        return reviewRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(review -> new ReviewSummary(
                        review.getId(),
                        review.getlanguage(),
                        review.getQualityScore(),
                        review.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        ReviewResult reviewResult = readResult(review.getReviewResult());
        return new ReviewResponse(review.getId(), review.getlanguage(), review.getSubmittedCode(), review.getCreatedAt(), reviewResult);
    }

    private String writeResult(ReviewResult reviewResult) {
        try {
            return objectMapper.writeValueAsString(reviewResult);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize review result", e);
            throw new IllegalStateException("Unable to persist AI result", e);
        }
    }

    private ReviewResult readResult(String payload) {
        try {
            return objectMapper.readValue(payload, ReviewResult.class);
        } catch (JsonProcessingException e) {
            log.error("Unable to deserialize review result", e);
            ReviewResult fallback = new ReviewResult();
            fallback.setRawResponse(payload);
            return fallback;
        }
    }
}
