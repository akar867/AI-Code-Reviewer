package com.example.dfs.repository;

import com.example.dfs.model.FileMetadata;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {

    Optional<FileMetadata> findByFilename(String filename);
}
