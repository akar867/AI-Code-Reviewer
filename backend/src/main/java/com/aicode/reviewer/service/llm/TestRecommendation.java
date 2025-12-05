package com.aicode.reviewer.service.llm;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestRecommendation {
    String filePath;
    String framework;
    String scenario;
    String example;
}
