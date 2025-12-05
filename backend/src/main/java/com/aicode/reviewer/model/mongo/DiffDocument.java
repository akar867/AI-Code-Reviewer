package com.aicode.reviewer.model.mongo;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "pull_request_diffs")
public class DiffDocument {
    @Id
    private String id;
    private Long reviewId;
    private Long githubPrId;
    private String repository;
    private String diff;
    private Map<String, Object> metadata;
    private Instant createdAt;
}
