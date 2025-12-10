package com.example.dfs.service;

import com.example.dfs.config.NodeDefinition;
import com.example.dfs.config.NodeProperties;
import com.example.dfs.config.StorageProperties;
import com.example.dfs.model.StorageNode;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeRegistry {

    private final NodeProperties nodeProperties;
    private final StorageProperties storageProperties;
    private final Map<String, StorageNode> nodes = new ConcurrentHashMap<>();

    @PostConstruct
    void boot() throws IOException {
        List<NodeDefinition> definitions = nodeProperties.getNodes().getDefinitions();
        if (definitions.isEmpty()) {
            definitions = defaultNodes();
        }

        for (NodeDefinition def : definitions) {
            if (!def.isEnabled()) {
                continue;
            }
            String nodeId = def.getId();
            if (!StringUtils.hasText(nodeId)) {
                throw new IllegalStateException("Node definition missing id");
            }
            String storagePath = determineStoragePath(def);
            ensureDirectory(storagePath);
            StorageNode node = StorageNode.builder()
                    .id(nodeId)
                    .name(StringUtils.hasText(def.getName()) ? def.getName() : nodeId)
                    .capacityGb(def.getCapacityGb())
                    .storagePath(storagePath)
                    .healthy(true)
                    .usedBytes(0L)
                    .lastHeartbeat(Instant.now())
                    .build();
            nodes.put(nodeId, node);
        }
        log.info("Registered {} storage nodes", nodes.size());
    }

    public List<StorageNode> listNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<StorageNode> pickNodes(int replicationFactor) {
        int factor = Math.min(replicationFactor, nodes.size());
        return nodes.values().stream()
                .filter(StorageNode::isHealthy)
                .sorted(Comparator.comparingLong(StorageNode::getUsedBytes))
                .limit(factor)
                .collect(Collectors.toList());
    }

    public void incrementUsage(String nodeId, long delta) {
        nodes.computeIfPresent(nodeId, (id, node) -> {
            node.setUsedBytes(Math.max(0, node.getUsedBytes() + delta));
            node.setLastHeartbeat(Instant.now());
            return node;
        });
    }

    public StorageNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    private String determineStoragePath(NodeDefinition def) {
        if (storageProperties.getMode() == StorageProperties.Mode.S3) {
            return def.getId();
        }
        if (StringUtils.hasText(def.getStoragePath())) {
            return def.getStoragePath();
        }
        return Path.of(storageProperties.getLocalBasePath(), def.getId()).toString();
    }

    private void ensureDirectory(String path) throws IOException {
        if (storageProperties.getMode() == StorageProperties.Mode.LOCAL) {
            Files.createDirectories(Path.of(path));
        }
    }

    private List<NodeDefinition> defaultNodes() {
        List<NodeDefinition> defaults = new ArrayList<>();
        defaults.add(createNode("node-a", "Alpha"));
        defaults.add(createNode("node-b", "Beta"));
        defaults.add(createNode("node-c", "Gamma"));
        return defaults;
    }

    private NodeDefinition createNode(String id, String name) {
        NodeDefinition def = new NodeDefinition();
        def.setId(id);
        def.setName(name);
        def.setCapacityGb(10);
        def.setEnabled(true);
        return def;
    }
}
