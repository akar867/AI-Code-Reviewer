package com.example.dfs.service;

import com.example.dfs.model.NodeCopy;
import com.example.dfs.model.StorageNode;
import com.example.dfs.service.storage.StorageProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplicationService {

    private final NodeRegistry nodeRegistry;
    private final StorageProvider storageProvider;

    public ReplicationResult replicate(String filename, long version, MultipartFile multipartFile, int replicationFactor)
            throws IOException {
        List<StorageNode> targets = nodeRegistry.pickNodes(replicationFactor);
        if (targets.isEmpty()) {
            throw new IOException("No storage nodes available");
        }

        Path tempFile = Files.createTempFile("dfs-upload", ".bin");
        MessageDigest digest = sha256();
        try (InputStream inputStream = new DigestInputStream(multipartFile.getInputStream(), digest);
                OutputStream outputStream = Files.newOutputStream(tempFile, StandardOpenOption.TRUNCATE_EXISTING)) {
            inputStream.transferTo(outputStream);
        }
        long payloadSize = Files.size(tempFile);
        String checksum = HexFormat.of().formatHex(digest.digest());

        String sanitized = sanitizeName(filename);
        List<NodeCopy> copies = new ArrayList<>();
        for (StorageNode node : targets) {
            String relativePath = sanitized + "/v" + version + "/" + multipartFile.getOriginalFilename();
            try (InputStream nodeStream = Files.newInputStream(tempFile)) {
                storageProvider.store(node, relativePath, nodeStream);
                copies.add(NodeCopy.builder()
                        .nodeId(node.getId())
                        .relativePath(relativePath)
                        .status("OK")
                        .build());
                nodeRegistry.incrementUsage(node.getId(), payloadSize);
            } catch (IOException ex) {
                log.error("Failed to replicate to node {}", node.getId(), ex);
                copies.add(NodeCopy.builder()
                        .nodeId(node.getId())
                        .relativePath(relativePath)
                        .status("FAILED")
                        .build());
            }
        }
        Files.deleteIfExists(tempFile);
        return ReplicationResult.builder()
                .checksum(checksum)
                .size(payloadSize)
                .copies(copies)
                .build();
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 missing", e);
        }
    }

    private String sanitizeName(String name) {
        if (!StringUtils.hasText(name)) {
            return "file" + Instant.now().toEpochMilli();
        }
        return name.replaceAll("[^a-zA-Z0-9-_]+", "_");
    }
}
