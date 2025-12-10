package com.example.dfs.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("file_metadata")
public class FileMetadata {
    @Id
    private String id;
    private String filename;
    private String contentType;
    private Instant createdAt;
    private Instant updatedAt;
    private String status;
    private long currentVersion;
    @lombok.Builder.Default
    private List<FileVersion> versions = new ArrayList<>();
    @lombok.Builder.Default
    private List<ConflictRecord> conflictLog = new ArrayList<>();
}
