package com.aicode.reviewer.model.entity;

import com.aicode.reviewer.enums.RiskLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "pull_request_reviews")
public class PullRequestReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long githubPrId;
    private String repoOwner;
    private String repoName;
    private Integer prNumber;
    private String title;
    private String author;

    @Column(length = 4000)
    private String summary;

    private double riskScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReviewFinding> findings = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TestCaseSuggestion> testSuggestions = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addFinding(ReviewFinding finding) {
        finding.setReview(this);
        findings.add(finding);
    }

    public void addTestSuggestion(TestCaseSuggestion suggestion) {
        suggestion.setReview(this);
        testSuggestions.add(suggestion);
    }
}
