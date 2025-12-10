package com.example.dfs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dfs.storage")
public class StorageProperties {

    public enum Mode {
        LOCAL,
        S3
    }

    private Mode mode = Mode.LOCAL;
    private String localBasePath = "storage";
    private final S3Properties s3 = new S3Properties();

    @Data
    public static class S3Properties {
        private String endpoint;
        private String region = "us-east-1";
        private String bucket;
        private String accessKey;
        private String secretKey;
    }
}
