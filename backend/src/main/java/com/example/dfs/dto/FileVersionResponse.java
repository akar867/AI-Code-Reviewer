package com.example.dfs.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileVersionResponse {
    long version;
    Instant createdAt;
    String checksum;
    long size;
    List<String> replicas;
}
