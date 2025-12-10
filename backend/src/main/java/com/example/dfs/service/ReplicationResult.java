package com.example.dfs.service;

import com.example.dfs.model.NodeCopy;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReplicationResult {
    String checksum;
    long size;
    List<NodeCopy> copies;
}
