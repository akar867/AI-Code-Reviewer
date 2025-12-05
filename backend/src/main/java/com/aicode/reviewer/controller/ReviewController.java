package com.aicode.reviewer.controller;

import com.aicode.reviewer.dto.ReviewDetailDto;
import com.aicode.reviewer.dto.ReviewSummaryDto;
import com.aicode.reviewer.dto.TestSuggestionDto;
import com.aicode.reviewer.enums.RiskLevel;
import com.aicode.reviewer.model.entity.PullRequestReview;
import com.aicode.reviewer.repository.jpa.PullRequestReviewRepository;
import com.aicode.reviewer.repository.jpa.TestCaseSuggestionRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final PullRequestReviewRepository reviewRepository;
    private final TestCaseSuggestionRepository testRepository;

    @GetMapping
    public List<ReviewSummaryDto> list(@RequestParam(name = "risk", required = false) RiskLevel riskLevel) {
        List<PullRequestReview> reviews = riskLevel == null
                ? reviewRepository.findAll()
                : reviewRepository.findAllByRiskLevelOrderByCreatedAtDesc(riskLevel);
        return reviews.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(ReviewSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ReviewDetailDto> detail(@PathVariable Long id) {
        Optional<PullRequestReview> review = reviewRepository.findById(id);
        return review.map(ReviewDetailDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/tests")
    public List<TestSuggestionDto> tests(@PathVariable Long id) {
        return testRepository.findByReviewId(id).stream()
                .map(TestSuggestionDto::fromEntity)
                .collect(Collectors.toList());
    }
}
