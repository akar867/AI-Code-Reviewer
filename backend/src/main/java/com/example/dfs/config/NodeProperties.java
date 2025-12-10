package com.example.dfs.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dfs")
public class NodeProperties {

    private final Nodes nodes = new Nodes();

    @Data
    public static class Nodes {
        private List<NodeDefinition> definitions = new ArrayList<>();
    }
}
