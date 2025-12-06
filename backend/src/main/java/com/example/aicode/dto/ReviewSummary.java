package com.example.aicode.dto;

import java.time.LocalDateTime;

public class ReviewSummary {

    private Long id;
    private String language;
    private Integer qualityScore;
    private LocalDateTime createdAt;

    public ReviewSummary() {
    }

    public ReviewSummary(Long id, String language, Integer qualityScore, LocalDateTime createdAt) {
        this.id = id;
        this.language = language;
        this.qualityScore = qualityScore;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
