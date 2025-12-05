package com.aicode.reviewer.repository.jpa;

import com.aicode.reviewer.model.entity.TestCaseSuggestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseSuggestionRepository extends JpaRepository<TestCaseSuggestion, Long> {
    List<TestCaseSuggestion> findByReviewId(Long reviewId);
}
