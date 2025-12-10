package com.example.dfs.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConflictResponse {
    long expectedVersion;
    long actualVersion;
    String actor;
    String strategy;
    Instant detectedAt;
    String note;
}
