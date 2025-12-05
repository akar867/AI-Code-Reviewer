package com.aicode.reviewer.dto;

import com.aicode.reviewer.model.entity.PullRequestReview;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewDetailDto {
    ReviewSummaryDto summary;
    List<FindingDto> findings;
    List<TestSuggestionDto> testSuggestions;

    public static ReviewDetailDto fromEntity(PullRequestReview review) {
        return ReviewDetailDto.builder()
                .summary(ReviewSummaryDto.fromEntity(review))
                .findings(review.getFindings().stream()
                        .map(FindingDto::fromEntity)
                        .collect(Collectors.toList()))
                .testSuggestions(review.getTestSuggestions().stream()
                        .map(TestSuggestionDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
