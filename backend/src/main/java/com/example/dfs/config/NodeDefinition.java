package com.example.dfs.config;

import lombok.Data;

@Data
public class NodeDefinition {
    private String id;
    private String name;
    private long capacityGb = 10;
    private boolean enabled = true;
    private String storagePath;
}
