package com.example.dfs.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileMetadataResponse {
    String filename;
    String contentType;
    long currentVersion;
    Instant updatedAt;
    List<FileVersionResponse> versions;
    List<String> conflicts;
}
