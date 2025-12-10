package com.example.dfs.controller;

import com.example.dfs.dto.NodeStatusResponse;
import com.example.dfs.model.StorageNode;
import com.example.dfs.service.NodeRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {

    private final NodeRegistry nodeRegistry;

    @GetMapping
    public List<NodeStatusResponse> nodes() {
        return nodeRegistry.listNodes().stream()
                .map(this::map)
                .toList();
    }

    private NodeStatusResponse map(StorageNode node) {
        double usagePercent = node.getCapacityGb() == 0
                ? 0
                : (double) node.getUsedBytes() / (node.getCapacityGb() * 1024 * 1024 * 1024) * 100;
        return NodeStatusResponse.builder()
                .id(node.getId())
                .name(node.getName())
                .healthy(node.isHealthy())
                .capacityGb(node.getCapacityGb())
                .usedBytes(node.getUsedBytes())
                .usagePercent(Math.min(100d, usagePercent))
                .lastHeartbeat(node.getLastHeartbeat())
                .build();
    }
}
