package com.aicode.reviewer.dto;

import com.aicode.reviewer.model.entity.TestCaseSuggestion;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestSuggestionDto {
    Long id;
    String filePath;
    String framework;
    String description;
    String example;

    public static TestSuggestionDto fromEntity(TestCaseSuggestion suggestion) {
        return TestSuggestionDto.builder()
                .id(suggestion.getId())
                .filePath(suggestion.getFilePath())
                .framework(suggestion.getFramework())
                .description(suggestion.getDescription())
                .example(suggestion.getExample())
                .build();
    }
}
