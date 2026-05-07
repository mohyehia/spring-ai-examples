package com.moh.yehia.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.stereotype.Component;

@Component
public class McpProgressHandler {
    private static final Logger log = LoggerFactory.getLogger(McpProgressHandler.class);

    @McpProgress(clients = "weather-service")
    void progressHandler(McpSchema.ProgressNotification progressNotification) {
        // Update UI or send websocket notification to frontend with the progress information
        String progressToken = progressNotification.progressToken().toString();
        log.info("Received progress update for the key {}: {}% - {}", progressToken, progressNotification.progress(), progressNotification.message());
    }

}
