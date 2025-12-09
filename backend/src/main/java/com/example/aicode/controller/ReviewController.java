package com.example.aicode.controller;

import com.example.aicode.dto.ReviewRequest;
import com.example.aicode.dto.ReviewResponse;
import com.example.aicode.dto.ReviewSummary;
import com.example.aicode.service.ReviewService;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;
   
    
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
		//this.githubDiffFetcher = null;
    }
    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Extract PR code + language (simplified example)
            Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
            if (pullRequest == null) return ResponseEntity.badRequest().body("No pull_request found");

         //   String body = (String) payload.get("code"); // You need to fetch actual code from repo or diff
          //  String language = (String) payload.get("language"); // Detect language or set default
            
            
            String body = (String) pullRequest.getOrDefault("body", null);
            Map<String, Object> head=  (Map<String, Object>) pullRequest.get("head");
            String ref= (String) head.get("ref");
            ReviewRequest request = new ReviewRequest();
            request.setCode(body);
            request.setRef(ref);

            reviewService.createReview(request);

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error processing webhook: " + ex.getMessage());
        }
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
