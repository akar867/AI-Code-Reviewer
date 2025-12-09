package com.example.aicode.dto;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private String ref;
    private String submittedCode;
    private LocalDateTime createdAt;
    private ReviewResult reviewResult;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id, String ref, String submittedCode, LocalDateTime createdAt, ReviewResult reviewResult) {
        this.id = id;
        this.ref = ref;
        this.submittedCode = submittedCode;
        this.createdAt = createdAt;
        this.reviewResult = reviewResult;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setLanguage(String ref) {
        this.ref = ref;
    }

    public String getSubmittedCode() {
        return submittedCode;
    }

    public void setSubmittedCode(String submittedCode) {
        this.submittedCode = submittedCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ReviewResult getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(ReviewResult reviewResult) {
        this.reviewResult = reviewResult;
    }
}
