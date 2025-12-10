package com.example.dfs.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictRecord {
    private long expectedVersion;
    private long actualVersion;
    private String actor;
    private ConflictStrategy strategy;
    private Instant detectedAt;
    private String note;
}
