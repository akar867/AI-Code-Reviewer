package com.aicode.reviewer.service;

import com.aicode.reviewer.config.ReviewerProperties;
import com.aicode.reviewer.diff.DiffFile;
import com.aicode.reviewer.diff.DiffParser;
import com.aicode.reviewer.enums.LineChangeType;
import com.aicode.reviewer.model.entity.PullRequestReview;
import com.aicode.reviewer.model.entity.ReviewFinding;
import com.aicode.reviewer.model.entity.TestCaseSuggestion;
import com.aicode.reviewer.model.mongo.DiffDocument;
import com.aicode.reviewer.repository.jpa.PullRequestReviewRepository;
import com.aicode.reviewer.repository.mongo.DiffDocumentRepository;
import com.aicode.reviewer.service.github.PullRequestContext;
import com.aicode.reviewer.service.llm.LLMClient;
import com.aicode.reviewer.service.llm.LLMReviewFinding;
import com.aicode.reviewer.service.llm.LLMReviewResult;
import com.aicode.reviewer.service.llm.TestRecommendation;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final DiffParser diffParser;
    private final LLMClient llmClient;
    private final RiskScoringService riskScoringService;
    private final PullRequestReviewRepository reviewRepository;
    private final DiffDocumentRepository diffDocumentRepository;
    private final ReviewerProperties reviewerProperties;

    @Transactional
    public PullRequestReview createAutomatedReview(PullRequestContext context, String diffContent) {
        List<DiffFile> parsedFiles = diffParser.parse(diffContent);
        List<DiffFile> truncated = parsedFiles.stream()
                .sorted(Comparator.comparing(file -> file.getHunks().size(), Comparator.reverseOrder()))
                .limit(Math.max(1, reviewerProperties.getMaxFiles()))
                .collect(Collectors.toList());

        LLMReviewResult result = llmClient.analyze(context.repoSlug(), context.prNumber(), truncated);

        PullRequestReview review = new PullRequestReview();
        review.setGithubPrId(context.pullRequestId());
        review.setRepoOwner(context.repoOwner());
        review.setRepoName(context.repoName());
        review.setPrNumber(context.prNumber());
        review.setTitle(context.title());
        review.setAuthor(context.author());
        review.setSummary(result.getSummary());

        result.getFindings().forEach(finding -> review.addFinding(toEntity(finding)));

        int additions = countChanges(truncated, LineChangeType.ADDED);
        int deletions = countChanges(truncated, LineChangeType.REMOVED);

        double riskScore = riskScoringService.calculateScore(review.getFindings(), result.getRiskScore(), truncated.size(), additions, deletions);
        review.setRiskScore(riskScore);
        review.setRiskLevel(riskScoringService.classify(riskScore));

        List<TestRecommendation> recommendations = llmClient.generateTests(context.repoSlug(), truncated);
        recommendations.forEach(rec -> review.addTestSuggestion(toEntity(rec)));

        PullRequestReview saved = reviewRepository.save(review);

        diffDocumentRepository.save(DiffDocument.builder()
                .reviewId(saved.getId())
                .githubPrId(context.pullRequestId())
                .repository(context.repoSlug())
                .diff(diffContent)
                .metadata(Map.of(
                        "files", truncated.size(),
                        "additions", additions,
                        "deletions", deletions))
                .createdAt(Instant.now())
                .build());

        return saved;
    }

    public List<DiffFile> parseDiff(String diffContent) {
        return diffParser.parse(diffContent);
    }

    private ReviewFinding toEntity(LLMReviewFinding finding) {
        ReviewFinding entity = new ReviewFinding();
        entity.setCategory(finding.getCategory());
        entity.setSeverity(finding.getSeverity());
        entity.setMessage(finding.getMessage());
        entity.setSuggestion(finding.getSuggestion());
        entity.setFilePath(finding.getFilePath());
        entity.setLineNumber(finding.getLineNumber());
        return entity;
    }

    private TestCaseSuggestion toEntity(TestRecommendation recommendation) {
        TestCaseSuggestion suggestion = new TestCaseSuggestion();
        suggestion.setFilePath(recommendation.getFilePath());
        suggestion.setFramework(recommendation.getFramework());
        suggestion.setDescription(recommendation.getScenario());
        suggestion.setExample(recommendation.getExample());
        return suggestion;
    }

    private int countChanges(List<DiffFile> files, LineChangeType type) {
        return files.stream()
                .flatMap(file -> file.getHunks().stream())
                .flatMap(hunk -> hunk.getChanges().stream())
                .filter(change -> change.getType() == type)
                .mapToInt(change -> 1)
                .sum();
    }
}
