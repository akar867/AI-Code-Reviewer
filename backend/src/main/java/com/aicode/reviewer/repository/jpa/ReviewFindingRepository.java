package com.aicode.reviewer.repository.jpa;

import com.aicode.reviewer.model.entity.ReviewFinding;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewFindingRepository extends JpaRepository<ReviewFinding, Long> {
    List<ReviewFinding> findByReviewId(Long reviewId);
}
