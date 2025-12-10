package com.example.dfs.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NodeStatusResponse {
    String id;
    String name;
    boolean healthy;
    long capacityGb;
    long usedBytes;
    double usagePercent;
    Instant lastHeartbeat;
}
