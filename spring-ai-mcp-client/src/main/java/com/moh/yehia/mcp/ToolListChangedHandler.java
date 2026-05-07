package com.moh.yehia.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpToolListChanged;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolListChangedHandler {

    private static final Logger log = LoggerFactory.getLogger(ToolListChangedHandler.class);

    @McpToolListChanged(clients = "weather-service")
    void handleToolListChanged(List<McpSchema.Tool> tools) {
        log.info("Received tool list change notification. Updated tools:");
        for (McpSchema.Tool tool : tools) {
            log.info("- {}: {}", tool.name(), tool.description());
        }
    }

//    @Bean
//    McpClientCustomizer<McpClient.SyncSpec> customizeMcpClient() {
//        return (name, mcpClientSpec) -> mcpClientSpec.toolsChangeConsumer(tv -> log.info("MCP TOOLS CHANGE: {}", tv));
//    }
}
