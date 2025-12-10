package com.example.dfs.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageNode {
    private String id;
    private String name;
    private long capacityGb;
    private String storagePath;
    private boolean healthy;
    private Instant lastHeartbeat;
    private long usedBytes;
}
