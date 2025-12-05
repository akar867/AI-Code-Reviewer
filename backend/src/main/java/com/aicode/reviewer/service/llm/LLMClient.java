package com.aicode.reviewer.service.llm;

import com.aicode.reviewer.diff.DiffFile;
import java.util.List;

public interface LLMClient {

    LLMReviewResult analyze(String repo, int prNumber, List<DiffFile> files);

    List<TestRecommendation> generateTests(String repo, List<DiffFile> files);
}
