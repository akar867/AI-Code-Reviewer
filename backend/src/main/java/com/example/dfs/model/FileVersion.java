package com.example.dfs.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileVersion {
    private long version;
    private Instant createdAt;
    private String checksum;
    private long size;
    private String uploadedBy;
    @lombok.Builder.Default
    private List<NodeCopy> copies = new ArrayList<>();
}
