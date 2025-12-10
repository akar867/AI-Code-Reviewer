package com.example.dfs.service.storage;

import com.example.dfs.model.StorageNode;
import com.example.dfs.service.StoredFileReference;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalStorageProvider implements StorageProvider {

    @Override
    public StoredFileReference store(StorageNode node, String relativePath, InputStream inputStream) throws IOException {
        Path target = nodePath(node).resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFileReference(node.getId(), relativePath);
    }

    @Override
    public Resource load(StorageNode node, String relativePath) throws IOException {
        Path target = nodePath(node).resolve(relativePath);
        if (!Files.exists(target)) {
            throw new IOException("File not found: " + target);
        }
        return new PathResource(target);
    }

    @Override
    public void delete(StorageNode node, String relativePath) throws IOException {
        Path target = nodePath(node).resolve(relativePath);
        Files.deleteIfExists(target);
    }

    private Path nodePath(StorageNode node) {
        return Path.of(node.getStoragePath());
    }
}
