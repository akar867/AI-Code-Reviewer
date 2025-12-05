package com.aicode.reviewer.repository.jpa;

import com.aicode.reviewer.enums.RiskLevel;
import com.aicode.reviewer.model.entity.PullRequestReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestReviewRepository extends JpaRepository<PullRequestReview, Long> {
    List<PullRequestReview> findAllByRiskLevelOrderByCreatedAtDesc(RiskLevel riskLevel);
}
