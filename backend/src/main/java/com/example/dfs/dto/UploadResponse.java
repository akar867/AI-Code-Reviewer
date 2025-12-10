package com.example.dfs.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadResponse {
    String filename;
    long version;
    String checksum;
    String message;
}
