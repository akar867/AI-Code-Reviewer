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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
	
	private final ReviewService reviewService;
    private final RestTemplate restTemplate;
    
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
		this.restTemplate = new RestTemplate();
    }

    @PostMapping("/submit")
    public ReviewResponse submitReview(@Valid @RequestBody ReviewRequest request) {
        return reviewService.createReview(request);
    }
   
    
    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
    	 try {
    	        Map<String, Object> pullRequest =
    	                (Map<String, Object>) payload.get("pull_request");

    	        if (pullRequest == null) {
    	            return ResponseEntity.badRequest().body("No pull_request found");
    	        }

    	        // 1️ Get patch URL
    	        String patchUrl = (String) pullRequest.get("patch_url");
    	        if (patchUrl == null) {
    	            return ResponseEntity.badRequest().body("No patch_url found");
    	        }

    	        // 2️ Prepare headers
    	        HttpHeaders headers = new HttpHeaders();
    	        headers.setBearerAuth(System.getenv("GITHUB_TOKEN"));
    	        headers.set("Accept", "application/vnd.github.v3.patch");

    	        HttpEntity<Void> entity = new HttpEntity<>(headers);

    	        // 3️ Fetch patch
    	        ResponseEntity<String> patchResponse =
    	                restTemplate.exchange(
    	                        patchUrl,
    	                        HttpMethod.GET,
    	                        entity,
    	                        String.class
    	                );

    	        String code = patchResponse.getBody();
    	        if (code == null || code.isBlank()) {
    	            return ResponseEntity.badRequest().body("Empty patch content");
    	        }

    	        // 4️⃣ Detect language
    	        String language = reviewService.detectLanguageFromPatch(code);

    	        ReviewRequest request = new ReviewRequest();
    	        request.setCode(code);
    	       // request.setl((String) pullRequest.get("url"));
    	        request.setlanguage(language);

    	        reviewService.createReview(request);

    	        return ResponseEntity.ok("Webhook processed successfully");

    	    } catch (Exception ex) {
    	        ex.printStackTrace();
    	        return ResponseEntity
    	                .status(500)
    	                .body("Error processing webhook: " + ex.getMessage());
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
