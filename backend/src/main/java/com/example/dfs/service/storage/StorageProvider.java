package com.example.dfs.service.storage;

import com.example.dfs.model.StorageNode;
import com.example.dfs.service.StoredFileReference;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface StorageProvider {

    StoredFileReference store(StorageNode node, String relativePath, InputStream inputStream) throws IOException;

    Resource load(StorageNode node, String relativePath) throws IOException;

    void delete(StorageNode node, String relativePath) throws IOException;
}
