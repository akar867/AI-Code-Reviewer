package com.example.dfs.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StoredFileReference {
    private String nodeId;
    private String relativePath;
}
