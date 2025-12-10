package com.example.dfs.config;

import com.example.dfs.service.storage.LocalStorageProvider;
import com.example.dfs.service.storage.S3StorageProvider;
import com.example.dfs.service.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StorageProviderConfig {

    private final StorageProperties storageProperties;
    private final LocalStorageProvider localStorageProvider;

    @Bean
    public StorageProvider storageProvider() {
        if (storageProperties.getMode() == StorageProperties.Mode.S3) {
            return new S3StorageProvider(storageProperties);
        }
        return localStorageProvider;
    }
}
