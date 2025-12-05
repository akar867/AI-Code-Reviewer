package com.aicode.reviewer.service.llm;

import com.aicode.reviewer.enums.FindingCategory;
import com.aicode.reviewer.enums.FindingSeverity;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LLMReviewFinding {
    FindingCategory category;
    FindingSeverity severity;
    String message;
    String suggestion;
    String filePath;
    Integer lineNumber;
}
