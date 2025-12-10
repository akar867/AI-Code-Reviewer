package com.example.dfs.controller;

import com.example.dfs.dto.ConflictResponse;
import com.example.dfs.dto.FileMetadataResponse;
import com.example.dfs.dto.FileVersionResponse;
import com.example.dfs.dto.UploadResponse;
import com.example.dfs.model.ConflictRecord;
import com.example.dfs.model.ConflictStrategy;
import com.example.dfs.model.FileMetadata;
import com.example.dfs.model.FileVersion;
import com.example.dfs.service.FileService;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "owner", required = false) String owner,
            @RequestParam(value = "replicationFactor", defaultValue = "2") int replicationFactor,
            @RequestParam(value = "expectedVersion", required = false) Long expectedVersion,
            @RequestParam(value = "strategy", defaultValue = "LAST_WRITE_WINS") ConflictStrategy strategy)
            throws IOException {
        String requestedName = file.getOriginalFilename();
        FileMetadata stored = fileService.upload(file, owner, replicationFactor, expectedVersion, strategy);
        String message = stored.getFilename().equals(requestedName)
                ? "Upload successful"
                : "Conflict detected. Stored as " + stored.getFilename();
        FileVersion latest = stored.getVersions().stream()
                .filter(v -> v.getVersion() == stored.getCurrentVersion())
                .findFirst()
                .orElseGet(() -> stored.getVersions().get(stored.getVersions().size() - 1));
        return UploadResponse.builder()
                .filename(stored.getFilename())
                .version(stored.getCurrentVersion())
                .checksum(latest.getChecksum())
                .message(message)
                .build();
    }

    @GetMapping
    public List<FileMetadataResponse> list() {
        return fileService.listFiles().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename,
            @RequestParam(value = "version", required = false) Long version) throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\\\"" + filename + "\\\"")
                .body(fileService.download(filename, version));
    }

    @GetMapping("/{filename}/conflicts")
    public List<ConflictResponse> conflicts(@PathVariable String filename) {
        return fileService.conflictLog(filename).stream()
                .map(this::toConflictResponse)
                .collect(Collectors.toList());
    }

    private FileMetadataResponse toResponse(FileMetadata metadata) {
        List<FileVersionResponse> versions = metadata.getVersions().stream()
                .map(version -> FileVersionResponse.builder()
                        .version(version.getVersion())
                        .createdAt(version.getCreatedAt())
                        .checksum(version.getChecksum())
                        .size(version.getSize())
                        .replicas(version.getCopies().stream()
                                .map(copy -> copy.getNodeId() + ":" + copy.getStatus())
                                .toList())
                        .build())
                .toList();
        List<String> conflictSummary = metadata.getConflictLog().stream()
                .map(record -> String.format("expected %d actual %d via %s", record.getExpectedVersion(),
                        record.getActualVersion(), record.getStrategy()))
                .toList();
        return FileMetadataResponse.builder()
                .filename(metadata.getFilename())
                .contentType(metadata.getContentType())
                .currentVersion(metadata.getCurrentVersion())
                .updatedAt(metadata.getUpdatedAt())
                .versions(versions)
                .conflicts(conflictSummary)
                .build();
    }

    private ConflictResponse toConflictResponse(ConflictRecord record) {
        return ConflictResponse.builder()
                .expectedVersion(record.getExpectedVersion())
                .actualVersion(record.getActualVersion())
                .actor(record.getActor())
                .strategy(record.getStrategy().name())
                .detectedAt(record.getDetectedAt())
                .note(record.getNote())
                .build();
    }
}
