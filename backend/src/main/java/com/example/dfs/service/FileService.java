package com.example.dfs.service;

import com.example.dfs.model.ConflictRecord;
import com.example.dfs.model.ConflictStrategy;
import com.example.dfs.model.FileMetadata;
import com.example.dfs.model.FileVersion;
import com.example.dfs.model.NodeCopy;
import com.example.dfs.model.StorageNode;
import com.example.dfs.repository.FileMetadataRepository;
import com.example.dfs.service.storage.StorageProvider;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository repository;
    private final ReplicationService replicationService;
    private final StorageProvider storageProvider;
    private final NodeRegistry nodeRegistry;
    private final FileLockManager lockManager;

    public FileMetadata upload(MultipartFile file, String owner, int replicationFactor, Long expectedVersion,
            ConflictStrategy strategy) throws IOException {
        String cleanName = StringUtils.cleanPath(file.getOriginalFilename());
        if (!StringUtils.hasText(cleanName)) {
            throw new IllegalArgumentException("Filename is required");
        }
        return uploadWithResolvedName(file, owner, replicationFactor, expectedVersion, strategy, cleanName);
    }

    private FileMetadata uploadWithResolvedName(MultipartFile file, String owner, int replicationFactor,
            Long expectedVersion, ConflictStrategy strategy, String filename) throws IOException {
        ReentrantLock lock = lockManager.lockFor(filename);
        lock.lock();
        boolean delegate = false;
        String delegateName = null;
        try {
            FileMetadata metadata = repository.findByFilename(filename)
                    .orElseGet(() -> createMetadata(filename, file.getContentType()));

            if (metadata.getVersions() == null) {
                metadata.setVersions(new java.util.ArrayList<>());
            }
            if (metadata.getConflictLog() == null) {
                metadata.setConflictLog(new java.util.ArrayList<>());
            }

            long currentVersion = metadata.getCurrentVersion();
            long expectation = expectedVersion != null ? expectedVersion : currentVersion;
            boolean conflictDetected = metadata.getId() != null && expectation != currentVersion;

            if (conflictDetected && strategy == ConflictStrategy.FAIL_FAST) {
                throw new IllegalStateException("Version conflict detected for " + filename);
            }

            if (conflictDetected && strategy == ConflictStrategy.KEEP_BOTH) {
                delegate = true;
                delegateName = filename + "-conflict-" + Instant.now().toEpochMilli();
                metadata.getConflictLog().add(ConflictRecord.builder()
                        .expectedVersion(expectation)
                        .actualVersion(currentVersion)
                        .actor(owner)
                        .strategy(strategy)
                        .detectedAt(Instant.now())
                        .note("Forked to " + delegateName)
                        .build());
                repository.save(metadata);
            } else {
                long nextVersion = metadata.getId() == null ? 1 : metadata.getCurrentVersion() + 1;
                ReplicationResult result = replicationService.replicate(filename, nextVersion, file, replicationFactor);
                FileVersion version = FileVersion.builder()
                        .version(nextVersion)
                        .createdAt(Instant.now())
                        .checksum(result.getChecksum())
                        .size(result.getSize())
                        .uploadedBy(owner)
                        .copies(result.getCopies())
                        .build();
                metadata.getVersions().add(version);
                metadata.setCurrentVersion(nextVersion);
                metadata.setContentType(file.getContentType());
                metadata.setUpdatedAt(Instant.now());

                if (conflictDetected) {
                    metadata.getConflictLog().add(ConflictRecord.builder()
                            .expectedVersion(expectation)
                            .actualVersion(currentVersion)
                            .actor(owner)
                            .strategy(strategy)
                            .detectedAt(Instant.now())
                            .note("Override applied")
                            .build());
                }
                return repository.save(metadata);
            }
        } finally {
            lock.unlock();
        }

        if (delegate && delegateName != null) {
            return uploadWithResolvedName(file, owner, replicationFactor, null, ConflictStrategy.LAST_WRITE_WINS,
                    delegateName);
        }
        throw new IllegalStateException("Failed to store file");
    }

    private FileMetadata createMetadata(String filename, String contentType) {
        return FileMetadata.builder()
                .filename(filename)
                .contentType(contentType)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .status("ACTIVE")
                .currentVersion(0)
                .versions(new java.util.ArrayList<>())
                .conflictLog(new java.util.ArrayList<>())
                .build();
    }

    public List<FileMetadata> listFiles() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(FileMetadata::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Resource download(String filename, Long version) throws IOException {
        FileMetadata metadata = repository.findByFilename(filename)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + filename));
        long desiredVersion = version != null ? version : metadata.getCurrentVersion();
        Optional<FileVersion> selected = metadata.getVersions().stream()
                .filter(v -> v.getVersion() == desiredVersion)
                .findFirst();
        FileVersion fileVersion = selected
                .orElseThrow(() -> new IllegalArgumentException("Version not available: " + desiredVersion));

        for (NodeCopy copy : fileVersion.getCopies()) {
            if (!Objects.equals(copy.getStatus(), "OK")) {
                continue;
            }
            StorageNode node = nodeRegistry.getNode(copy.getNodeId());
            if (node == null) {
                continue;
            }
            try {
                return storageProvider.load(node, copy.getRelativePath());
            } catch (IOException ex) {
                // try next copy
            }
        }
        throw new IOException("Unable to fetch file from any replica");
    }

    public List<ConflictRecord> conflictLog(String filename) {
        return repository.findByFilename(filename)
                .map(FileMetadata::getConflictLog)
                .orElse(List.of());
    }
}
