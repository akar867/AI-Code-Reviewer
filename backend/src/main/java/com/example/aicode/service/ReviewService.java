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
import org.springframework.stereotype.Service;

import java.util.List;
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

    public ReviewResponse createReview(ReviewRequest request) {
        ReviewResult reviewResult = aiReviewService.reviewCode(request.getCode(), request.getLanguage());
        Review review = new Review();
        review.setLanguage(request.getLanguage());
        review.setSubmittedCode(request.getCode());
        review.setQualityScore(reviewResult.getQualityScore());
        review.setReviewResult(writeResult(reviewResult));
        Review saved = reviewRepository.save(review);
        return new ReviewResponse(saved.getId(), saved.getLanguage(), saved.getSubmittedCode(), saved.getCreatedAt(), reviewResult);
    }

    public List<ReviewSummary> getRecentReviews() {
        return reviewRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(review -> new ReviewSummary(
                        review.getId(),
                        review.getLanguage(),
                        review.getQualityScore(),
                        review.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        ReviewResult reviewResult = readResult(review.getReviewResult());
        return new ReviewResponse(review.getId(), review.getLanguage(), review.getSubmittedCode(), review.getCreatedAt(), reviewResult);
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
