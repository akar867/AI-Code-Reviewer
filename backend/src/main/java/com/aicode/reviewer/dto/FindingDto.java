package com.aicode.reviewer.dto;

import com.aicode.reviewer.enums.FindingCategory;
import com.aicode.reviewer.enums.FindingSeverity;
import com.aicode.reviewer.model.entity.ReviewFinding;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FindingDto {
    Long id;
    FindingCategory category;
    FindingSeverity severity;
    String filePath;
    Integer lineNumber;
    String message;
    String suggestion;

    public static FindingDto fromEntity(ReviewFinding finding) {
        return FindingDto.builder()
                .id(finding.getId())
                .category(finding.getCategory())
                .severity(finding.getSeverity())
                .filePath(finding.getFilePath())
                .lineNumber(finding.getLineNumber())
                .message(finding.getMessage())
                .suggestion(finding.getSuggestion())
                .build();
    }
}
