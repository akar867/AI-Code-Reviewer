package com.example.aicode.controller;

import com.example.aicode.dto.ReviewRequest;
import com.example.aicode.dto.ReviewResponse;
import com.example.aicode.dto.ReviewSummary;
import com.example.aicode.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ReviewSummary> listRecent() {
        return reviewService.getRecentReviews();
    }

    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable Long id) {
        return reviewService.getReview(id);
    }
}
